(ns status-im.contexts.keycard.error.view
  (:require [quo.core :as quo]
            [status-im.common.events-helper :as events-helper]
            [status-im.contexts.keycard.factory-reset.view :as factory-reset]
            [status-im.contexts.keycard.unblock.view :as keycard.unblock]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def titles
  {:keycard/error.keycard-unpaired {:title       (i18n/label :t/all-slots-occupied)
                                    :description (i18n/label :t/cant-use-keycard-device)}
   :keycard/error.keycard-frozen   {:title       (i18n/label :t/keycard-blocked)
                                    :description (i18n/label :t/cant-use-right-now)}
   :keycard/error.keycard-locked   {:title       (i18n/label :t/keycard-blocked)
                                    :description (i18n/label :t/cant-use-right-now)}})

(defn view
  []
  (let [error                       (rf/sub [:keycard/application-info-error])
        {:keys [title description]} (get titles error)]
    (fn []
      [:<>
       [quo/page-nav
        {:icon-name :i/close
         :on-press  events-helper/navigate-back}]
       [quo/page-top
        {:title            title
         :description      :text
         :description-text description}]
       [quo/keycard]
       [quo/category
        {:list-type :settings
         :label (i18n/label :t/what-you-can-do)
         :blur? true
         :data
         [(when (or (= error :keycard/error.keycard-frozen)
                    (= error :keycard/error.keycard-locked))
            {:title             (i18n/label :t/unblock-keycard)
             :image             :icon
             :image-props       :i/unlocked
             :action            :arrow
             :description       :text
             :description-props {:text (i18n/label :t/with-recovery-phrase)}
             :on-press          (fn []
                                  (rf/dispatch [:show-bottom-sheet
                                                {:content keycard.unblock/sheet}]))})
          {:title             (i18n/label :t/factory-reset)
           :image             :icon
           :image-props       :i/revert
           :action            :arrow
           :description       :text
           :description-props {:text (i18n/label :t/remove-keycard-content)}
           :on-press          (fn []
                                (rf/dispatch [:show-bottom-sheet
                                              {:theme   :dark
                                               :shell?  true
                                               :content factory-reset/sheet}]))}]}]])))
