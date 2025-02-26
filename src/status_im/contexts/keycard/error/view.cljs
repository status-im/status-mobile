(ns status-im.contexts.keycard.error.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.contexts.keycard.backup.view :as backup.view]
            [status-im.contexts.keycard.factory-reset.view :as factory-reset]
            [status-im.contexts.keycard.unblock.view :as keycard.unblock]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def titles
  {:keycard/error.keycard-empty         {:title       (i18n/label :t/keycard-empty)
                                         :description (i18n/label :t/no-key-pair-keycard)}
   :keycard/error.keycard-wrong-profile {:title       (i18n/label :t/keycard-not-empty)
                                         :description (i18n/label :t/cant-store-new-keys)}
   :keycard/error.keycard-unpaired      {:title       (i18n/label :t/keycard-full)
                                         :description (i18n/label :t/pairing-slots-occupied)}
   :keycard/error.keycard-frozen        {:title       (i18n/label :t/keycard-blocked)
                                         :description (i18n/label :t/cant-use-right-now)}
   :keycard/error.keycard-locked        {:title       (i18n/label :t/keycard-blocked)
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
      [quo/keycard {:blur? true}]
      [quo/section-label
       {:section (i18n/label :t/what-you-can-do) :container-style {:padding-vertical 8}}]
      (if (= error :keycard/error.keycard-empty)
        [quo/settings-item
         {:title             (i18n/label :t/use-backup-keycard)
          :image             :icon
          :image-props       :i/placeholder
          :action            :arrow
          :description       :text
          :description-props {:text (i18n/label :t/create-backup-profile-keycard)}
          :on-press          (fn []
                               (rf/dispatch [:show-bottom-sheet
                                             {:content
                                              (fn []
                                                [backup.view/sheet
                                                 {:on-continue
                                                  (rf/dispatch
                                                   [:keycard/backup.create-or-enter-pin])}])}]))}]
        [:<>
         (when (or (= error :keycard/error.keycard-frozen)
                   (= error :keycard/error.keycard-locked))
           [quo/settings-item
            {:title             (i18n/label :t/unblock-keycard)
             :image             :icon
             :image-props       :i/placeholder
             :action            :arrow
             :description       :text
             :description-props {:text (i18n/label :t/with-recovery-phrase)}
             :on-press          (fn []
                                  (rf/dispatch [:show-bottom-sheet
                                                {:content keycard.unblock/sheet}]))}])
         [quo/settings-item
          {:title             (i18n/label :t/factory-reset)
           :image             :icon
           :image-props       :i/placeholder
           :action            :arrow
           :description       :text
           :description-props {:text (i18n/label :t/remove-keycard-content)}
           :on-press          (fn []
                                (rf/dispatch [:show-bottom-sheet
                                              {:theme   :dark
                                               :shell?  true
                                               :content factory-reset/sheet}]))}]])]]))
