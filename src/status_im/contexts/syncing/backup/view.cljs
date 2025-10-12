(ns status-im.contexts.syncing.backup.view
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im.contexts.syncing.backup.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.transforms :as types]))

(defn go-back
  []
  (rf/dispatch [:navigate-back]))

(defn share-backup-file
  [result]
  (let [parsed-result (types/json->clj result)
        file-path     (:filePath parsed-result)]
    (if (and file-path (not (string/blank? file-path)))
      (do
        (rf/dispatch [:toasts/upsert
                      {:type :positive
                       :text (i18n/label :t/backup-completed)}])
        (rf/dispatch [:syncing/share-backup-file file-path]))
      (rf/dispatch [:toasts/upsert
                    {:type :negative
                     :text (str "Backup failed: " (or (:error parsed-result) "no file path"))}]))))

(defn on-toggle-messages-backup
  [enabled?]
  (if enabled?
    (rf/dispatch [:profile.settings/show-messages-backup-confirmation])
    (rf/dispatch [:profile.settings/set-messages-backup-enabled false])))

(defn view
  []
  (let [profile-color            (rf/sub [:profile/customization-color])
        messages-backup-enabled? (rf/sub [:profile/messages-backup-enabled?])]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:type       :no-title
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   go-back}]
     [rn/scroll-view
      {:content-container-style         style/page-container
       :style                           {:flex 1}
       :shows-vertical-scroll-indicator false}
      [rn/view {:style style/title-container}
       [quo/text
        {:size   :heading-1
         :weight :semi-bold
         :style  {:color colors/white}}
        (i18n/label :t/backup)]]
      [quo/category
       {:blur? true
        :list-type :settings
        :container-style style/category-container
        :data
        [{:title        (i18n/label :t/backup-messages-locally)
          :blur?        true
          :action       :selector
          :action-props {:type      :toggle
                         :checked?  messages-backup-enabled?
                         :on-change #(on-toggle-messages-backup %)}}]}]
      [quo/button
       {:type                :primary
        :background          :blur
        :size                40
        :customization-color profile-color
        :on-press            #(native-module/perform-local-backup share-backup-file)}
       (i18n/label :t/backup-data-locally)]]]))
