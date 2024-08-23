(ns status-im.contexts.preview.quo.info.info-message
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :status
    :type    :select
    :options [{:key :default}
              {:key :success}
              {:key :error}
              {:key :warning}]}
   {:key     :size
    :type    :select
    :options [{:key :default}
              {:key :tiny}]}
   {:key  :blur?
    :type :boolean}
   {:key     :icon
    :type    :select
    :options [{:key :i/placeholder}
              {:key :i/info}]}
   {:key  :message
    :type :text}])

(defn view
  []
  (let [[state set-state] (rn/use-state
                           {:status  :default
                            :size    :default
                            :message "This is a message"
                            :blur?   false
                            :icon    :i/placeholder})
        blur?             (:blur? state)]
    [preview/preview-container
     {:state                 state
      :descriptor            descriptor
      :blur-dark-only?       true
      :show-blur-background? blur?
      :blur?                 blur?
      :set-state             set-state}
     [quo/info-message (dissoc state :message) (:message state)]]))
