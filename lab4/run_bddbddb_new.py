"""
Original: oldpython2scripts/run_bddbddb.py 

stolen from original code by Michael Martin
modified by Philip Guo

Updated by Yancheng Ou in order to run on Python3 
"""
import sys
import os 
import os.path
import re 
import subprocess

current_file_dir = os.path.dirname(os.path.realpath(__file__))
"""
set pql_home path to environment variable PQLHOME, 
if doesn't exist it's the path containing this file 
"""
pql_home = os.getenv(
    "PQLHOME", 
    current_file_dir
)
java_home = os.getenv("JAVA_HOME")
if java_home is None:
    print("JAVA_HOME not set! Please set it to your java 1.5 path")
    exit(1)
"""
The path to your version 1.5-compatible java 
"""
java_cmd = java_home+"/bin/java"
# "java" 

cleanup = True 

"""
The classpath of java 
"""
basecp = os.getenv("CLASSPATH", current_file_dir).split(os.path.pathsep) 

print("the base classpath is: {}".format(basecp)) 

verbose = True 

pql_jar = None
jline_jar = None

def check_env_vars(): 
    global pql_home, pql_jar, jline_jar 
    pql_ok = (
        pql_home is not None and 
        os.path.exists(os.path.join(pql_home, "PQL-0.2.jar"))
    )
    if not pql_ok: 
        print("PQLHOME is not set or the current file directory is not containing PQL-0.2.jar")
        exit(1)
    pql_jar = os.path.join(pql_home, "PQL-0.2.jar")
    jline_jar = os.path.join(pql_home, "jline.jar")


def clean(result_dir = "results"): 
    if not result_dir.endswith(os.path.sep): 
        result_dir += os.path.sep 
    for (root, subs, files) in os.walk(result_dir, False): 
        for f in files: 
            if verbose:
                print("Deleting file", os.path.join(root, f))
                os.remove(os.path.join(root, f))
        if verbose: 
            print("deleting directory", root)
        os.rmdir(root)


def gen_relations(main_class, path_dirs, result_dir="results"): 
    if not result_dir.endswith(os.path.sep): 
        result_dir += os.path.sep
    classpath = os.path.pathsep.join([current_file_dir, pql_jar] + path_dirs)
    cmd = '{} -cp "{}" -Xmx1024m -Dpa.dumppath={} -Dpa.specialmapinfo=yes -Dpa.dumpunmunged=yes -Dpa.signaturesinlocs=yes joeq.Main.GenRelations {}'\
        .format(java_cmd, classpath, result_dir, main_class)
    if verbose:
        print(cmd)

    p = subprocess.Popen(
        cmd, 
        shell=True, 
        stdin=subprocess.PIPE, 
        stdout=subprocess.PIPE, 
        stderr=subprocess.STDOUT,
        close_fds=True
    )
    (in_p, out_p) = (p.stdin, p.stdout)
    result_str = out_p.read() 
    out_p.close()
    in_p.close()
    if verbose:
        print(result_str.decode("ascii"))
    cmd = '%s -mx1024m -Dbasedir=%s -Dresultdir=%s -Dpa.discovercallgraph=yes -cp "%s" net.sf.bddbddb.BuildEquivalenceRelation H0 H0 heap.map I0 invoke.map' % (java_cmd, result_dir, result_dir, classpath)
    if verbose:
        print(cmd)
    p = subprocess.Popen(
        cmd, 
        shell=True, 
        stdin=subprocess.PIPE, 
        stdout=subprocess.PIPE, 
        stderr=subprocess.STDOUT,
        close_fds=True
    )
    (in_p, out_p) = (p.stdin, p.stdout)
    result_str = out_p.read() 
    out_p.close()
    if verbose:
        print(result_str.decode("ascii"))
    os.system("mv map_* results/")

def datalog_solve(program, filename, numberingtype="scc", omit_stderr=False): 
    f = open(filename, "wt")
    print(program, file=f)
    f.close()
    classpath = os.path.pathsep.join([pql_jar, current_file_dir])
    cmd = '%s -cp "%s" -mx1024m -Dlearnbestorder=no -Dsingleignore=yes -Dbasedir=./results/ -Dbddcache=1500000 -Dbddnodes=40000000 -Dnumberingtype=%s -Dpa.clinit=no -Dpa.filternull=yes -Dpa.unknowntypes=no net.sf.bddbddb.Solver %s' % (java_cmd, classpath, numberingtype, filename)
    if verbose:
        print(cmd)
    
    stderr_pos = subprocess.DEVNULL if omit_stderr else subprocess.STDOUT
    p = subprocess.Popen(cmd, shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=stderr_pos, close_fds=True)
    (in_p, out_p) = (p.stdin, p.stdout)
    result_str = out_p.read()
    out_p.close()
    in_p.close()
    if cleanup: 
        os.remove(filename)
    print(result_str.decode("ascii"))
    return result_str

def datalog_solve_filename(program_filename, numberingtype="scc"):
    classpath = os.path.pathsep.join ([pql_jar, current_file_dir])
    cmd = '%s -cp "%s" -mx1024m -Dlearnbestorder=no -Dsingleignore=yes -Dbasedir=./results/ -Dbddcache=1500000 -Dbddnodes=40000000 -Dnumberingtype=%s -Dpa.clinit=no -Dpa.filternull=yes -Dpa.unknowntypes=no net.sf.bddbddb.Solver %s' % (java_cmd, classpath, numberingtype, program_filename)
    if verbose:
        print(cmd)
    p = subprocess.Popen(cmd, shell=True, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, close_fds=True)
    (in_p, out_p) = (p.stdin, p.stdout)
    result_str = out_p.read()
    out_p.close()
    in_p.close()
    print(result_str.decode("ascii"))
    return result_str


numbering = """# -*- Mode: C; indent-tabs-mode: nil; c-basic-offset: 4 -*-
### Context-sensitive inclusion-based pointer analysis using cloning
# 
# Calculates the numbering based on the call graph relation.
# 
# Author: John Whaley

.basedir "results"
.include "fielddomains.pa"

.bddnodes 10000000
.bddcache 1000000

# found by findbestorder:
#.bddvarorder G0_C0_C1_N0_F0_I0_M1_M0_V1xV0_VC1xVC0_T0_Z0_T1_H0_H1
.bddvarorder C0_C1_N0_F0_I0_M1_M0_V1xV0_VC1xVC0_T0_Z0_T1_H0_H1_G0

### Relations

mI (method:M0, invoke:I0, name:N0) input
IE0 (invoke:I0, target:M0) input

roots (method:M0) input

mI0 (method:M0, invoke:I0)
IEnum (invoke:I0, target:M0, ccaller:VC1, ccallee:VC0) output

### Rules

mI0(m,i) :- mI(m,i,_).
IEnum(i,m,vc2,vc1) :- roots(m), mI0(m,i), IE0(i,m). number
"""


"""
Note: there will be an exception thrown because domain is not patched
"""
def fix_contexts():
    print("------------------------------------ fix_contexts ---------------------------------")
    f_str = datalog_solve (numbering, "number.dtl", omit_stderr=False)
    # print("f_str is ... ", f_str)
    m = re.search(r"paths = (\d+)", f_str.decode("ascii"))
    if m is not None:
        paths = m.group(1)
        print("Requires %s paths." % paths)
        domains = []
        i = open(os.path.join("results", "fielddomains.pa"), "rt")
        for l in i:
            if l.startswith("VC "):
                domains.append("VC %s" % paths)
            else:
                domains.append(l.strip())
        i.close()
        i = open(os.path.join("results", "fielddomains.pa"), "wt")
        for l in domains:
            print(l, file=i)
        i.close()
    print("------------------------ NOTE: an exception here is normal ------------------------")
    print("-----------------------------------------------------------------------------------")



dumptypes = """.basedir "results"

### Domains

.include "fielddomains.pa"

.bddvarorder M1_I0_N0_F0_M0_V1xV0_H1_Z0_T0_T1_H0
### Relations

aT (type1:T0, type2:T1) input outputtuples
"""

def run_pql(pql_query):
    datalog_solve (dumptypes, "dumpat.dtl")
    pql_cp = os.path.pathsep.join([current_file_dir, pql_jar])
    cmd = "%s -cp %s -Dpql.datalog.pacs=no net.sf.pql.datalog.DatalogGenerator %s" % (java_cmd, pql_cp, pql_query)
    if verbose:
        print(cmd)

    p = subprocess.Popen(cmd, 
        shell=True, 
        stdin=subprocess.PIPE, 
        stdout=subprocess.PIPE, 
        stderr=subprocess.PIPE, 
        close_fds=True
    )
    (i_p, o_p, e_p) = (p.stdin, p.stdout, p.stderr)
    dtl = o_p.read()
    problems = e_p.read()
    print(problems)
    datalog_solve (dtl, "pql_conv.dtl")

def tuple_map(src, names, target): 
    src = os.path.join(current_file_dir, "results", src)
    names = os.path.join(current_file_dir, "results", names)
    n = [x.strip() for x in open(names)]
    l = [n[int(x)] for x in open(src) if x[0] != '#' and int(x) < len(n)]
    o = open(target, "wt")
    for x in l:
        print(x, file=o)
    o.close()

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python3 ./run_bddbddb.py <main_class> <datalog_filename>")
        print("(Fast mode - if you've already gen'ed relations): python3 ./run_bddbddb.py <main_class> <datalog_filename> --fast")
        sys.exit(1)

    check_env_vars()

    # Michael tells me to split these parts up so that students can make
    # multiple queries without regenerating this stuff every time ...

    if len(sys.argv) < 4 or sys.argv[3] != '--fast':
        print("cleaning up...")
        clean()
        print("Generating relations...")
        gen_relations (sys.argv[1], basecp)
        print("De-munging names ...")
        os.system('mv results/unmunged_method.map results/method.map')
        os.system('mv results/unmunged_name.map results/name.map')
    else:
        os.system('rm results/*.tuples')

  
    print("Preparing for running context-sensitive pointer analysis ...")

    print("Counting contexts...")
    fix_contexts()
    
    print("Numbering contexts...")
    datalog_solve (numbering, "numbering.dtl")

    print("Running analysis in file ...")
    datalog_solve_filename(sys.argv[2])

    print("Done")
    sys.exit(0)

