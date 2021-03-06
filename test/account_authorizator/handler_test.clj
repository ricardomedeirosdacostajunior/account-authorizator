(ns account-authorizator.handler-test
  (:require [clojure.test :refer [deftest, is, , use-fixtures]]
            [ring.mock.request :as mock]
            [account-authorizator.handler :refer [app]]
            [account-authorizator.helper.account_helper :refer [clear-database]]))

(defn get-expected-account []
  "{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 }, \"violations\": [] }")

(defn get-expected-account-after-transaction []
  "{ \"account\": { \"activeCard\": true, \"availableLimit\": 90 }, \"violations\": [] }")

(defn get-expected-already-initialized-account []
  "{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 }, \"violations\": [\"account-already-initialized\"] }")

(defn get-expected-transactions []
  "[{\"merchant\":\"Saitama\",\"amount\":10,\"time\":\"2019-02-13T11:00:00.000Z\"}]")

(defn account-post-request []
  (app (-> (mock/request :post "/accounts" "{ \"account\": { \"activeCard\": true, \"availableLimit\": 100 } }")
           (mock/content-type "application/json"))))

(defn transaction-post-request []
  (app (-> (mock/request :post "/transactions" "{ \"transaction\": { \"merchant\": \"Saitama\", \"amount\": 10,\"time\": \"2019-02-13T11:00:00.000Z\" } }")
           (mock/content-type "application/json"))))          

(deftest post-account-without-previous-account
    (let [response (account-post-request)
          body     (:body response)]
      (is (= (:status response) 200))
      (is (= body (get-expected-account)))))

(deftest post-account-with-previous-account
    (account-post-request)
    (let [response (account-post-request)
          body     (:body response)]
      (is (= (:status response) 200))
      (is (= body (get-expected-already-initialized-account)))))

(deftest get-accounts
    (account-post-request)
    (let [response (app (-> (mock/request :get "/accounts")))
          body     (:body response)]
      (is (= (:status response) 200))
      (is (= body (get-expected-account)))))

(deftest post-transaction-with-previous-account
  (account-post-request)
  (let [response (transaction-post-request)
        body     (:body response)]
    (is (= (:status response) 200))
    (is (= body (get-expected-account-after-transaction)))))

(deftest get-transactions
  (account-post-request)
  (transaction-post-request)
  (let [response (app (-> (mock/request :get "/transactions")))
        body     (:body response)]
    (is (= (:status response) 200))
    (is (= body (get-expected-transactions)))))

(use-fixtures :each clear-database)