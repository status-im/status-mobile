(ns status-im.data-store.browser
  (:require [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]))

(re-frame/reg-cofx
 :data-store/all-browsers
 (fn [cofx _]
   (assoc cofx :all-stored-browsers (-> @core/account-realm
                                        (core/get-all :browser)
                                        (core/sorted :timestamp :desc)
                                        (core/all-clj :browser)))))

(defn save-browser-tx
  "Returns tx function for saving browser"
  [{:keys [browser-id] :as browser}]
  (fn [realm]
    (core/create realm :browser browser (core/exists? realm :browser :browser-id browser-id))))

(defn remove-browser-tx
  "Returns tx function for removing browser"
  [browser-id]
  (fn [realm]
    (let [browser (core/single (core/get-by-field realm :browser :browser-id browser-id))]
      (core/delete realm browser))))
