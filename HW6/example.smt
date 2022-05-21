; Example from the instructions
(set-logic ALL)
(set-option :produce-models true)
(declare-const x (_ BitVec 32))
(declare-const y (_ BitVec 32))
(assert (= x (bvadd y #x00000001)))
(check-sat)
(get-model)
