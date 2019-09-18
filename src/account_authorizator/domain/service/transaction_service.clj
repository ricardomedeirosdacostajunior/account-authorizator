(ns account-authorizator.domain.service.transaction_service
    (:require 
             [account-authorizator.domain.service.account_service :refer [create]]
             [account-authorizator.domain.entity.account_entity :refer [->Account]]
             [account-authorizator.domain.entity.transaction_entity :refer [same-sorted-transactions, happened-in-two-minutes]]))

(defn remaining-limit [availableLimit, amount]
    (- availableLimit amount))

(defn build-account-with-error [account, error]
    (->Account (:activeCard account) (:availableLimit account) [error]))

(defn make-transaction-with-actived-card [account, transaction]
    (let [remaining-limit
        (remaining-limit (:availableLimit account) (:amount transaction))]
        (if (>= remaining-limit 0)
            (create true remaining-limit)
            (build-account-with-error account "insufficient-limit"))))

(defn is-double-transaction [past-transactions, transaction]
    (let [sorted-transactions (same-sorted-transactions past-transactions transaction)]
    (if (not (empty? sorted-transactions))
        (happened-in-two-minutes (last sorted-transactions) transaction))))

(defn make-transaction
    ([account, transaction] (if (not (:activeCard account))
                                    (build-account-with-error account "card-not-active")
                                    (make-transaction-with-actived-card account transaction)))
    ([past-transactions, account, transaction] (if (is-double-transaction past-transactions transaction)
                                                        (build-account-with-error account "double-transaction")
                                                        (make-transaction account transaction))))