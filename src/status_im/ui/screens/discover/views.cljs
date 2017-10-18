(ns status-im.ui.screens.discover.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.components.react :as react]
            [status-im.components.icons.vector-icons :as vector-icons]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.components.toolbar-new.actions :as actions]
            [status-im.components.drawer.view :as drawer]
            [status-im.components.carousel.carousel :as carousel]
            [status-im.ui.screens.discover.components.views :as components]
            [status-im.ui.screens.discover.all-dapps.views :as all-dapps]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.ui.screens.contacts.styles :as contacts-st]
            [status-im.components.list.views :as list]
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

(defn toolbar-view [show-search? search-text]
  [toolbar/toolbar-with-search
   {:show-search?       show-search?
    :search-text        search-text
    :search-key         :discover
    :title              (i18n/label :t/discover)
    :search-placeholder (i18n/label :t/search-tags)
    :nav-action         (actions/hamburger drawer/open-drawer!)
    :on-search-submit   (fn [text]
                          (when-not (string/blank? text)
                            (let [hashtags (get-hashtags text)]
                              ;; TODO (goranjovic) - refactor double dispatch to a single call
                              (re-frame/dispatch [:set :discover-search-tags hashtags])
                              (re-frame/dispatch [:navigate-to :discover-search-results]))))}])


(defview top-status-for-popular-hashtag [{:keys [tag current-account]}]
  (letsubs [discoveries [:get-popular-discoveries 1 [tag]]]
    [react/view styles/popular-list-container
     [react/view styles/row
      [react/view {}
       ;; TODO (goranjovic) - refactor double dispatch to a single call
       [react/touchable-highlight {:on-press #(do (re-frame/dispatch [:set :discover-search-tags [tag]])
                                                  (re-frame/dispatch [:navigate-to :discover-search-results]))}
        [react/view {}
         [react/text {:style styles/tag-name
                      :font  :medium}
          (str " #" (name tag))]]]]
      [react/view styles/tag-count-container
       [react/text {:style styles/tag-count
                    :font  :default}
        (str (:total discoveries))]]]
     [components/discover-list-item {:message         (first (:discoveries discoveries))
                                     :show-separator? false
                                     :current-account current-account}]]))

(defview popular-hashtags-preview [{:keys [contacts current-account]}]
  (letsubs [popular-tags [:get-popular-tags 10]]
    (let [has-content? (seq popular-tags)]
      [react/view styles/popular-container
       ;; TODO (goranjovic) - refactor double dispatch to a single call
       [components/title :t/popular-tags :t/all #(do (re-frame/dispatch [:set :discover-search-tags (map :name popular-tags)])
                                                     (re-frame/dispatch [:navigate-to :discover-all-hashtags])) has-content?]
       (if has-content?
         [carousel/carousel {:pageStyle styles/carousel-page-style
                             :gap       8
                             :sneak     16
                             :count     (count popular-tags)}
          (for [{:keys [name]} popular-tags]
            [top-status-for-popular-hashtag {:tag             name
                                             :contacts        contacts
                                             :current-account current-account}])]
         [empty-section :empty-hashtags :t/no-hashtags-discovered-title :t/no-hashtags-discovered-body])])))

(defn recent-statuses-preview [current-account discoveries]
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
                                           :current-account current-account}]])]
       [empty-section :empty-recent :t/no-statuses-discovered :t/no-statuses-discovered-body])]))

(def public-chats-mock-data
  [{:name  "Status team"
    :count 25
    :color "#B2F3E3"}
   {:name  "ETH news"
    :count 12
    :color "#F7A7E8"}
   {:name  "All about Ethereum"
    :count 32
    :color "#C1B8F0"}])

(defn render-public-chats-item [item]
  [react/view styles/public-chats-item-container
   [react/view styles/public-chats-icon-container
    [react/view (styles/public-chats-icon (:color item))
     [react/text {:style styles/public-chats-icon-text}
                 (-> item :name first str)]]]
   [react/view styles/public-chats-item-inner
    [react/view styles/public-chats-item-name-container
     ;; TODO(goranjovic) lightgray intentionally hardcoded while only a teaser
     ;; will be removed and properly styled when enabled
     [vector-icons/icon :icons/public {:color "lightgray"}]
     [react/text {:font  :medium
                  :style styles/public-chats-item-name-text}
                 (:name item)]]
    [react/view {}
     [react/text {:style {:color :lightgray}}
      (i18n/label :t/public-chat-user-count {:count (:count item)})]]]])

(defn public-chats-teaser []
  [react/view styles/public-chats-container
   [components/title :t/public-chats :t/soon #() false]
   [list/flat-list {:data      public-chats-mock-data
                    :render-fn render-public-chats-item}]])

(defview discover [current-view?]
  (letsubs [show-search     [:get-in [:toolbar-search :show]]
            search-text     [:get-in [:toolbar-search :text]]
            contacts        [:get-contacts]
            current-account [:get-current-account]
            discoveries     [:get-recent-discoveries]
            all-dapps       [:get-all-dapps]]
    [react/view styles/discover-container
     [toolbar-view (and current-view?
                        (= show-search :discover)) search-text]
       [react/scroll-view styles/list-container
        [recent-statuses-preview current-account discoveries]
        [popular-hashtags-preview {:contacts        contacts
                                   :current-account current-account}]
        [all-dapps/preview all-dapps]
        [public-chats-teaser]]]))
