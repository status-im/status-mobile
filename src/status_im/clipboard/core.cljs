(ns status-im.clipboard.core
  (:require ["react-native" :refer (NativeModules)]
            [re-frame.core :as re-frame]
            [status-im.utils.types :as types]
            [quo.platform :as platform]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(def clipboard-manager (when platform/ios? (.-MediaClipboard ^js NativeModules)))

(def prefix-count (count "data:image/jpeg;base64,"))

(defn copy-image [base64 on-success on-error]
  (when platform/ios?
    (let [image (subs base64 prefix-count (count base64))]
      (-> (.copyImage ^js clipboard-manager image)
          (.then (comp on-success types/js->clj))
          (.catch (comp on-error types/js->clj))))))

(defn paste [on-success on-error]
  (when platform/ios?
    (-> (.paste ^js clipboard-manager)
        (.then (comp on-success types/js->clj))
        (.catch (comp on-error types/js->clj)))))

(defn has-image [on-success on-error]
  (when platform/ios?
    (-> (.hasImages ^js clipboard-manager)
        (.then (comp on-success types/js->clj))
        (.catch (comp on-error types/js->clj)))))

(re-frame/reg-fx
 ::copy-image
 (fn [base64]
   (copy-image base64
               #(log/info "[clipboard-manager] Image copy success" %)
               #(log/warn "[clipboard-manager] Image copy error" %))))

(re-frame/reg-fx
 ::has-image
 (fn []
   (has-image #(re-frame/dispatch [::image-clipboard-state %])
              #(log/warn "[clipboard-manager] Has image error" %))))

(re-frame/reg-fx
 ::paste
 (fn [callback]
   (paste callback
          #(log/warn "[clipboard-manager] paste error" %))))

(fx/defn image-clipboard-state
  {:events [::image-clipboard-state]}
  [{:keys [db]} state]
  {:db (assoc-in db [:clipboard :last-image-state] state)})

(fx/defn copy-image-event
  {:events [::copy-image]}
  [_ base64]
  {::copy-image base64})

(fx/defn paste-image-event
  {:events [::paste]}
  [_ on-success]
  {::paste on-success})
