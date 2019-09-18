(ns account-authorizator.domain.service.transaction_service_test
    (:require 
             [clojure.test :refer [deftest, is]]
             [account-authorizator.domain.entity.account_entity :refer [->Account]]
             [account-authorizator.domain.entity.transaction_entity :refer [->Transaction]]
             [account-authorizator.domain.service.transaction_service :refer [make-transaction]]))

(require '[clj-time.core :as t])
(require '[clj-time.format :as f])

(defn get-iso-date [date]
    (f/parse (f/formatters :date-time) date))

(deftest must-make-a-transaction
    (is (= (->Account true 0 [])
           (make-transaction 
                (->Account true 100 []) 
                (->Transaction "Naruto Uzumaki" 100 (t/now))))))

(deftest must-not-make-a-transaction-without-limit
    (is (= (->Account true 20 ["insufficient-limit"])
           (make-transaction
                (->Account true 20 [])
                (->Transaction "Sasuke Uchiha" 80 (t/now))))))

(deftest must-not-make-a-transaction-with-not-active-card
    (is (= (->Account false 100 ["card-not-active"])
           (make-transaction
                (->Account false 100 [])
                (->Transaction "Itachi Uchiha" 666 (t/now))))))

(defn get-transaction-list-with-double-transactions []
    [(->Transaction "Itachi Uchiha" 12 (get-iso-date "2019-02-13T11:00:00.000Z")),
     (->Transaction "Itachi Uchiha" 32 (get-iso-date "2019-02-13T11:00:00.000Z")),
     (->Transaction "Madara Uchiha" 666 (get-iso-date "2019-02-13T11:00:00.000Z")),
     (->Transaction "Itachi Uchiha" 666 (get-iso-date "2019-02-13T11:00:00.000Z")),
     (->Transaction "Madara Uchiha" 666 (get-iso-date "2019-02-13T11:00:01.000Z"))])

(defn get-transaction-list-with-high-frequency-small-interval []
    [(->Transaction "Blast" 12 (get-iso-date "2019-02-13T11:02:01.000Z")),
     (->Transaction "Mumem Rider" 32 (get-iso-date "2019-02-13T12:00:00.000Z")),
     (->Transaction "Zombieman" 666 (get-iso-date "2019-02-13T11:01:00.000Z")),
     (->Transaction "Amai Mask" 666 (get-iso-date "2019-02-13T11:01:10.000Z")),
     (->Transaction "Saitama" 666 (get-iso-date "2019-02-13T16:02:01.000Z"))])

(deftest must-not-make-a-transaction-if-there-is-one-similar-in-two-minutes-ago
    (is (= (->Account true 100 ["double-transaction"])
           (make-transaction
                (get-transaction-list-with-double-transactions)
                (->Account true 100 [])
                (->Transaction "Itachi Uchiha" 666 (get-iso-date "2019-02-13T11:00:00.000Z"))))))

(deftest must-make-a-transaction-if-there-is-not-one-similar-in-two-minutes-ago
    (is (= (->Account true 1 [])
           (make-transaction
                [(->Transaction "Madara Uchiha" 10 (t/now))]
                (->Account true 100 [])
                (->Transaction "Itachi Uchiha" 99 (t/now))))))

(deftest must-not-make-a-transaction-if-happens-three-transactions-in-2-minutes-interval
    (is (= (->Account true 100 ["double-transaction"])
           (make-transaction
                (get-transaction-list-with-double-transactions)
                (->Account true 100 [])
                (->Transaction "Itachi Uchiha" 666 (get-iso-date "2019-02-13T11:00:00.000Z"))))))