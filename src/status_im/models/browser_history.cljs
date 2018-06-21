(ns status-im.models.browser-history
  (:require [re-frame.core :as re-frame]))

(defn dont-store-history-on-nav-change? [db]
  (get-in db [:browser/options :dont-store-history-on-nav-change?]))

(defn dont-store-history-on-nav-change! []
  (re-frame/dispatch [:update-browser-options {:dont-store-history-on-nav-change? true}]))

(defn clear-dont-store-history-on-nav-change! []
  (re-frame/dispatch [:update-browser-options {:dont-store-history-on-nav-change? false}]))

(defn dont-store-history-on-nav-change-if-history-exists [db browser-id]
  (let [browsers (get-in db [:browser/browsers])
        browser (get browsers browser-id)]
    (hash-map :dont-store-history-on-nav-change? (some? (:history browser)))))

(defn back [browser]
  (let [back-index (dec (:history-index browser))
        back-url (nth (:history browser) back-index)]
    (dont-store-history-on-nav-change!)
    (re-frame/dispatch [:update-browser (-> browser (assoc :url back-url :history-index back-index))])))

(defn forward [browser]
  (let [forward-index (inc (:history-index browser))
        forward-url (nth (:history browser) forward-index)]
    (dont-store-history-on-nav-change!)
    (re-frame/dispatch [:update-browser (-> browser (assoc :url forward-url :history-index forward-index))])))

(defn can-go-back? [browser]
  (let [hi (:history-index browser)]
    (and (some? hi) (not= hi 0))))

(defn can-go-forward? [browser]
  (let [hi (:history-index browser)]
    (and (some? hi)
         (< hi (dec (count (:history browser)))))))

(defn record-history-in-browser-if-needed [db raw-browser url loading]
  (let [browser (assoc raw-browser :url url)]
    (cond
      loading
      browser

      (dont-store-history-on-nav-change? db)
      (do (clear-dont-store-history-on-nav-change!)
          browser)

      :else
      (let [history-index (:history-index browser)
            history (or (:history browser) [])
            history-url (if history-index (nth history history-index) nil)
            history-to-index (if history-index (subvec history 0 (inc history-index)) [])
            new-history (if (not= history-url url) (conj history-to-index url) history)
            new-index (dec (count new-history))]
        (assoc browser :history new-history :history-index new-index)))))
