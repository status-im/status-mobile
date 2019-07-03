(ns status-im.chaos-mode.core
  (:require [status-im.utils.fx :as fx]
            [re-frame.core :as re-frame]
            [status-im.utils.utils :as utils]
            [status-im.utils.http :as http]
            [status-im.utils.types :as types]))

(def interval-5m (* 5 60 1000))
(defonce interval-id (atom nil))
(def url "https://cloudflare-dns.com/dns-query?name=chaos-unicorn-day.status.im&type=TXT")

(defn chaos-mode-switched [chaos-mode?]
  (re-frame/dispatch [:multiaccounts.ui/chaos-mode-switched chaos-mode?]))

(defn handle-response [response]
  (let [status-id   (:Status (types/json->clj response))
        chaos-mode? (zero? status-id)]
    (chaos-mode-switched chaos-mode?)))

(defn check-record []
  (http/get url
            handle-response
            (fn [])
            nil
            {"accept" "application/dns-json"}))

(re-frame/reg-fx
 :chaos-mode/start-checking
 (fn []
   (when @interval-id
     (utils/clear-interval @interval-id))
   (check-record)
   (reset!
    interval-id
    (utils/set-interval check-record interval-5m))))

(re-frame/reg-fx
 :chaos-mode/stop-checking
 (fn []
   (utils/clear-interval @interval-id)))

(fx/defn check-chaos-mode [_]
  {:chaos-mode/start-checking nil})

(fx/defn stop-checking [_]
  {:chaos-mode/stop-checking nil})
