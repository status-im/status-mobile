(ns status-im.models.browser
  (:require [status-im.data-store.browser :as browser-store]))

(defn get-current-url [{:keys [history history-index]}]
  (when (and history-index history)
    (nth history history-index)))

(defn can-go-back? [{:keys [history-index]}]
  (pos? history-index))

(defn can-go-forward? [{:keys [history-index history]}]
  (< history-index (dec (count history))))

(defn update-browser-fx [{:keys [db now]} browser]
  (let [updated-browser (assoc browser :timestamp now)]
    {:db            (update-in db [:browser/browsers (:browser-id updated-browser)]
                               merge updated-browser)
     :data-store/tx [(browser-store/save-browser-tx updated-browser)]}))

(defn update-browser-history-fx [cofx browser url loading?]
  (when-not loading?
    (let [history-index (:history-index browser)
          history       (:history browser)
          history-url   (try (nth history history-index) (catch js/Error _))]
      (when (not= history-url url)
        (let [slash? (= url (str history-url "/"))
              new-history (if slash?
                            (assoc history history-index url)
                            (conj (subvec history 0 (inc history-index)) url))
              new-index   (if slash?
                            history-index
                            (dec (count new-history)))]
          (update-browser-fx cofx
                             (assoc browser :history new-history :history-index new-index)))))))

(defn update-browser-and-navigate [cofx browser]
  (merge (update-browser-fx cofx browser)
         {:dispatch [:navigate-to :browser (:browser-id browser)]}))