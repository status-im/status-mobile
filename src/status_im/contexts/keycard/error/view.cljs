(ns status-im.contexts.keycard.error.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def titles
  {:keycard/error.keycard-blank         {:title       (i18n/label :t/keycard-empty)
                                         :description (i18n/label :t/no-key-pair-keycard)}
   :keycard/error.keycard-wrong-profile {:title       (i18n/label :t/keycard-not-empty)
                                         :description (i18n/label :t/cant-store-new-keys)}
   :keycard/error.keycard-unpaired      {:title       (i18n/label :t/keycard-full)
                                         :description (i18n/label :t/pairing-slots-occupied)}
   :keycard/error.keycard-frozen        {:title       (i18n/label :t/keycard-locked)
                                         :description (i18n/label :t/cant-use-right-now)}
   :keycard/error.keycard-locked        {:title       (i18n/label :t/keycard-locked)
                                         :description (i18n/label :t/cant-use-right-now)}})

(defn view
  []
  (let [error                       (rf/sub [:keycard/application-info-error])
        {:keys [title description]} (get titles error)]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title            title
       :description      :text
       :description-text description}]
     [rn/view {:style {:margin-horizontal 20}}
      [quo/keycard {:holder-name ""}]
      [quo/information-box
       {:type  :default
        :style {:margin-top 20}}
       (i18n/label :t/unlock-reset-instructions)]]]))
