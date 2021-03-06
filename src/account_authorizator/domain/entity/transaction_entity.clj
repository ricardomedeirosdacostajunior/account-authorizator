(ns account-authorizator.domain.entity.transaction_entity)

(require '[clj-time.core :as t])

(defrecord Transaction [merchant, amount, time])

(defn equals [transaction, anotherTransaction]
    (and (= (:merchant transaction) (:merchant anotherTransaction))
         (= (:amount transaction) (:amount anotherTransaction))))

(defn happened-in-two-minutes [transaction, anotherTransaction]
    (<= (t/in-minutes (t/interval (:time transaction) (:time anotherTransaction)) )
        2))

(defn same-sorted-transactions [past-transactions, transaction]
    (sort-by :time (filter #(equals % transaction) past-transactions)))

(defn is-high-frequency-small-interval [past-transactions, transaction]
    (>= (count (filter #(happened-in-two-minutes % transaction) past-transactions))
        3))