(ns status-im.ui.screens.link-previews-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [quo.core :as quo]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.link-previews-settings.styles :as styles]
            [status-im.chat.models.link-preview :as link-preview]))

(defn prepare-urls-items-data [link-previews-enabled-sites]
  (fn [{:keys [title address]}]
    (let [enabled? (contains? link-previews-enabled-sites title)]
      {:title     title
       :subtitle  address
       :size      :small
       :accessory :switch
       :active    (contains? link-previews-enabled-sites title)
       :on-press #(re-frame/dispatch
                   [::link-preview/enable title ((complement boolean) enabled?)])})))

(views/defview link-previews-settings []
  (views/letsubs [link-previews-whitelist [:link-preview/whitelist]
                  link-previews-enabled-sites [:link-preview/enabled-sites]]
    [react/view {:flex 1}
     [topbar/topbar {:title (i18n/label :t/chat-link-previews)}]
     [react/image {:source      (resources/get-theme-image :unfurl)
                   :style       styles/link-preview-settings-image}]
     [quo/text {:style {:margin 16}}
      (i18n/label :t/you-can-choose-preview-websites)]
     [quo/separator {:style {:margin-vertical  8}}]

     [react/view styles/whitelist-container
      [quo/list-header (i18n/label :t/websites)]

      (when (> (count link-previews-whitelist) 1)
        [quo/button {:on-press #(doseq [site (map :title link-previews-whitelist)]
                                  (re-frame/dispatch
                                   [::link-preview/enable site true]))
                     :type     :secondary
                     :style styles/enable-all}
         (i18n/label :t/enable-all)])]

     [list/flat-list
      {:data      (vec (map (prepare-urls-items-data link-previews-enabled-sites) link-previews-whitelist))
       :key-fn    (fn [_ i] (str i))
       :render-fn quo/list-item
       :footer [quo/text {:color :secondary
                          :style {:margin 16}} (i18n/label :t/previewing-may-share-metadata)]}]]))
