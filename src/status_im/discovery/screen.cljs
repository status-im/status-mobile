(ns status-im.discovery.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [clojure.string :as str]
    [status-im.components.react :refer [view
                                        scroll-view
                                        text
                                        text-input
                                        icon]]
    [status-im.components.toolbar.view :refer [toolbar]]
    [status-im.components.drawer.view :refer [open-drawer]]
    [status-im.discovery.styles :as st]
    [status-im.components.tabs.bottom-gradient :refer [bottom-gradient]]
    [status-im.i18n :refer [label]]
    [status-im.components.carousel.carousel :refer [carousel]]
    [status-im.discovery.views.popular-list :refer [discovery-popular-list]]
    [status-im.discovery.views.discovery-list-item :refer [discovery-list-item]]
    [status-im.contacts.styles :as contacts-styles]))

(defn get-hashtags [status]
  (let [hashtags (map #(str/lower-case (str/replace % #"#" "")) (re-seq #"[^ !?,;:.]+" status))]
    (or hashtags [])))

(defn title-content [show-search?]
  [view st/discovery-toolbar-content
   (if show-search?
     [text-input {:style           st/discovery-search-input
                  :autoFocus       true
                  :placeholder     (label :t/search-tags)
                  :onSubmitEditing (fn [e]
                                     (let [search   (aget e "nativeEvent" "text")
                                           hashtags (get-hashtags search)]
                                       (dispatch [:set :discovery-search-tags hashtags])
                                       (dispatch [:navigate-to :discovery-search-results])))}]
     [view
      [text {:style st/discovery-title
             :font  :default}
       (label :t/discovery)]])])

(defn toogle-search [current-value]
  (dispatch [:set ::show-search? (not current-value)]))

(defn discovery-toolbar [show-search?]
  [toolbar
   {:style          st/discovery-toolbar
    :nav-action     {:image   {:source {:uri :icon_hamburger}
                               :style  st/hamburger-icon}
                     :handler open-drawer}
    :custom-content [title-content show-search?]
    :actions        [{:image   {:source {:uri :icon_search}
                                :style  st/search-icon}
                      :handler #(toogle-search show-search?)}]}])

(defn title [label-kw]
  [view st/section-spacing
   [text {:style st/discovery-subtitle
          :font  :medium}
    (label label-kw)]])

(defview discovery-popular [{:keys [contacts]}]
  [popular-tags [:get-popular-tags 10]]
  (if (seq popular-tags)
    [view
     [title :t/popular-tags]
     (if (pos? (count popular-tags))
       [carousel {:pageStyle st/carousel-page-style}
        (for [{:keys [name count]} popular-tags]
          [discovery-popular-list {:tag      name
                                   :count    count
                                   :contacts contacts}])]
       [text (label :t/none)])]
    [view contacts-styles/empty-contact-groups
     ;; todo change icon
     [icon :group_big contacts-styles/empty-contacts-icon]
     [text {:style contacts-styles/empty-contacts-text}
      (label :t/no-statuses-discovered)]]))

(defview discovery-recent [{:keys [contacts]}]
  [discoveries [:get :discoveries]]
  (when (seq discoveries)
    [view
     [title :t/recent]
     [view st/recent-list
      (for [{:keys [message-id] :as discovery} discoveries]
        ^{:key (str "message-" message-id)}
        [discovery-list-item discovery])]]))

(defview discovery []
  [show-search? [:get ::show-search?]
   contacts [:get :contacts]]
  [view st/discovery-container
   [discovery-toolbar show-search?]
   [scroll-view st/scroll-view-container
    [discovery-popular {:contacts contacts}]
    [discovery-recent {:contacts contacts}]]
   [bottom-gradient]])
