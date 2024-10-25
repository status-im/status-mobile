(ns status-im.contexts.keycard.error.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.events-helper :as events-helper]
            [utils.re-frame :as rf]))

(def titles
  {:keycard/error.keycard-wrong    {:title       "Keycard is not empty"
                                    :description "You can’t use it to store new keys right now"}
   :keycard/error.keycard-unpaired {:title       "Keycard is full"
                                    :description "All pairing slots are occupied"}
   :keycard/error.keycard-frozen   {:title       "Keycard is locked"
                                    :description "You can’t use it right now"}
   :keycard/error.keycard-locked   {:title       "Keycard is locked"
                                    :description "You can’t use it right now"}})

(defn view
  []
  (let [{:keys [top bottom]}        (safe-area/get-insets)
        error                       (rf/sub [:keycard/application-info-error])
        {:keys [title description]} (get titles error)]
    [quo/overlay
     {:type            :shell
      :container-style {:padding-top    top
                        :padding-bottom bottom}}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top
      {:title            title
       :description      :text
       :description-text description}]
     [rn/view {:height 226}]
     [rn/view {:padding-horizontal 20}
      [quo/info-message
       {:container-style {:padding-top 15}
        :icon            :i/info
        :size            :default}
       "To unlock or factory reset the Keycard, please use the Status desktop app. If you'd like this features on mobile, feel free to upvote them and discuss in the Status community."]]]))
