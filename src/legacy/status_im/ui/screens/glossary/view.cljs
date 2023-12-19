(ns legacy.status-im.ui.screens.glossary.view
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [utils.i18n :as i18n]))

(def messages
  [{:title   :t/account-title
    :content :t/account-content}
   {:title   :t/wallet-key-title
    :content :t/wallet-key-content}
   {:title   :t/chat-key-title
    :content :t/chat-key-content}
   {:title   :t/chat-name-title
    :content :t/chat-name-content}
   {:title   :t/ens-name-title
    :content :t/ens-name-content}
   {:title   :t/mailserver-title
    :content :t/mailserver-content}
   {:title   :t/peer-title
    :content :t/peer-content}
   {:title   :t/seed-phrase-title
    :content :t/seed-phrase-content}])

(defn render-section-header
  [{:keys [title]}]
  [react/view
   {:style {:position         "absolute"
            :width            24
            :padding-vertical 16
            :background-color colors/white}}
   [react/text
    {:style {:color       colors/blue
             :font-weight "700"}}
    title]])

(defn render-element
  [{:keys [title content]}]
  [react/view
   {:style {:margin-left 24
            :margin-top  16}}
   [react/text
    {:style {:font-weight   "700"
             :margin-bottom 6}}
    (i18n/label title)]
   [react/text
    (i18n/label content)]])

(defn glossary
  []
  (let [sections (->> messages
                      (group-by (comp first i18n/label :title))
                      seq
                      (sort-by first)
                      (map (fn [[k v]]
                             {:title k
                              :data  v})))]
    [list/section-list
     {:contentContainerStyle       {:padding-horizontal 16
                                    :padding-bottom     16}
      :stickySectionHeadersEnabled true
      :sections                    sections
      :render-fn                   render-element
      :render-section-header-fn    render-section-header}]))
