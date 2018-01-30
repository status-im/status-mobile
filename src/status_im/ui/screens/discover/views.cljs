(ns status-im.ui.screens.discover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.carousel.carousel :as carousel]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.ui.screens.discover.all-dapps.views :as all-dapps]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.ui.screens.contacts.styles :as contacts-st]
            [status-im.ui.components.list.views :as list]
            [status-im.react-native.resources :as resources]))

(defn empty-section [image-kw title-kw body-kw]
  [react/view styles/empty-section-container
   [react/image {:source (image-kw resources/ui)
                 :style  styles/empty-section-image}]
   [react/view styles/empty-section-description
    [react/text {:font  :medium
                 :style styles/empty-section-title-text}
     (i18n/label title-kw)]
    [react/text {:style styles/empty-section-body-text}
     (i18n/label body-kw)]]])

(defn get-hashtags [status]
  (let [hashtags (map #(string/lower-case (string/replace % #"#" "")) (re-seq #"[^ !?,;:.]+" status))]
    (or hashtags [])))

(defn top-status-for-popular-hashtag [{:keys [popular-hashtag current-account contacts]}]
  (let [{:keys [tag discovery total]} popular-hashtag]
    [react/view styles/popular-list-container
     [react/view styles/row
      [react/view {}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:discover/search-tag-results-view tag])}
        [react/view {}
         [react/text {:style styles/tag-name
                      :font  :medium}
          (str " #" (name tag))]]]]
      [react/view styles/tag-count-container
       [react/text {:style styles/tag-count
                    :font  :default}
        (str total)]]]
     [components/discover-list-item {:message         discovery
                                     :show-separator? false
                                     :current-account current-account
                                     :contacts        contacts}]]))

(defn popular-hashtags-preview [{:keys [popular-hashtags contacts current-account]}]
  (let [has-content? (seq popular-hashtags)]
    [react/view styles/popular-container
     [components/title :t/popular-tags :t/all #(re-frame/dispatch [:navigate-to :discover-all-popular-hashtags]) has-content?]
     (if has-content?
       [carousel/carousel {:pageStyle styles/carousel-page-style
                           :gap       8
                           :sneak     16
                           :count     (count popular-hashtags)}
        (for [popular-hashtag popular-hashtags]
          [top-status-for-popular-hashtag {:popular-hashtag         popular-hashtag
                                           :contacts                contacts
                                           :current-account         current-account}])]
       [empty-section :empty-hashtags :t/no-hashtags-discovered-title :t/no-hashtags-discovered-body])]))

(defn recent-statuses-preview [{:keys [current-account contacts discoveries]}]
  (let [has-content? (seq discoveries)]
    [react/view styles/recent-statuses-preview-container
     [components/title :t/recent :t/all #(re-frame/dispatch [:navigate-to :discover-all-recent]) has-content?]
     (if has-content?
       [carousel/carousel {:pageStyle styles/carousel-page-style
                           :gap       8
                           :sneak     16
                           :count     (count discoveries)}
        (for [discovery discoveries]
          [react/view styles/recent-statuses-preview-content
           [components/discover-list-item {:message         discovery
                                           :show-separator? false
                                           :current-account current-account
                                           :contacts        contacts}]])]
       [empty-section :empty-recent :t/no-statuses-discovered :t/no-statuses-discovered-body])]))

;; TODO(oskarth): Figure out chat count how to get from public chat list subscription
;; TODO(oskarth): Move colors into common namespace
(def public-chats-mock-data
  [{:name  "Status"
    :topic "status"
    :count 25
    :color "#77DCC6"}
   {:name  "ETH news"
    :topic "ethnews"
    :count 12
    :color "#DC77CE"}
   {:name  "All about Ethereum"
    :topic "ethereum"
    :count 32
    :color "#778CDC"}
   {:name  "Devcon"
    :topic "devcon"
    :count 47
    :color "#77DCC6"}])

(defn navigate-to-public-chat [topic]
  (re-frame/dispatch [:create-new-public-chat topic]))

(defn render-public-chats-item [{:keys [name color topic] :as item}]
  [react/touchable-highlight {:on-press #(navigate-to-public-chat topic)}
   [react/view styles/public-chats-item-container
    [react/view styles/public-chats-icon-container
     [react/view (styles/public-chats-icon color)
      [react/text {:style styles/public-chats-icon-text}
       (-> name first str)]]]
    [react/view styles/public-chats-item-inner
     [react/view styles/public-chats-item-name-container
      [vector-icons/icon :icons/public-chat]
      [react/text {:font  :medium
                   :style styles/public-chats-item-name-text}
       name]]
     [react/view {}
      [react/text {:style {:color :lightgray}}
       (str "#" topic)]]]]])

(defn public-chats-teaser []
  [react/view styles/public-chats-container
   [components/title-no-action :t/public-chats]
   [list/flat-list {:data      public-chats-mock-data
                    :render-fn render-public-chats-item}]])

(defview discover [current-view?]
  (letsubs [contacts            [:get-contacts]
            current-account     [:get-current-account]
            discoveries         [:discover/recent-discoveries]
            all-dapps           [:discover/all-dapps]
            popular-hashtags    [:discover/popular-hashtags-preview]]
    [react/view styles/discover-container
     [toolbar/simple-toolbar (i18n/label :t/discover)]
     [react/scroll-view styles/list-container
      [recent-statuses-preview {:contacts        contacts
                                :current-account current-account
                                :discoveries     discoveries}]
      [popular-hashtags-preview {:popular-hashtags popular-hashtags
                                 :contacts         contacts
                                 :current-account  current-account}]
      [all-dapps/preview all-dapps]
      [public-chats-teaser]]]))
