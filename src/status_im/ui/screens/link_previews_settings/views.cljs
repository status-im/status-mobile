(ns status-im.ui.screens.link-previews-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [quo.core :as quo]
            [re-frame.core :as re-frame]
            [status-im.chat.models.link-preview :as link-preview]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.link-previews-settings.styles :as styles]))

(defn prepare-urls-items-data
  [link-previews-enabled-sites]
  (fn [{:keys [title address]}]
    (let [enabled? (contains? link-previews-enabled-sites title)]
      {:title     title
       :subtitle  address
       :size      :small
       :accessory :switch
       :active    (contains? link-previews-enabled-sites title)
       :on-press  #(re-frame/dispatch
                    [::link-preview/enable title ((complement boolean) enabled?)])})))

(views/defview link-previews-settings
  []
  (views/letsubs [link-previews-whitelist     [:link-previews-whitelist]
                  link-previews-enabled-sites [:link-preview/enabled-sites]]
    (let [all-enabled (= (count link-previews-whitelist) (count link-previews-enabled-sites))]
      [:<>
       [react/image
        {:source (resources/get-theme-image :unfurl)
         :style  styles/link-preview-settings-image}]
       [quo/text {:style {:margin 16}}
        (i18n/label :t/you-can-choose-preview-websites)]
       [quo/separator {:style {:margin-vertical 8}}]

       [react/view styles/whitelist-container
        [quo/list-header (i18n/label :t/websites)]

        (when (> (count link-previews-whitelist) 1)
          [quo/button
           {:on-press #(re-frame/dispatch [::link-preview/enable-all
                                           link-previews-whitelist
                                           (not all-enabled)])
            :type     :secondary
            :style    styles/enable-all}
           (if all-enabled
             (i18n/label :t/disable-all)
             (i18n/label :t/enable-all))])]
       [list/flat-list
        {:data      (vec (map (prepare-urls-items-data link-previews-enabled-sites)
                              link-previews-whitelist))
         :key-fn    (fn [_ i] (str i))
         :render-fn quo/list-item
         :footer    [quo/text
                     {:color :secondary
                      :style {:margin 16}} (i18n/label :t/previewing-may-share-metadata)]}]])))
