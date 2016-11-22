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
    [status-im.contacts.styles :as contacts-styles]
    [status-im.utils.platform :refer [platform-specific]]))

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
             :font  :toolbar-title}
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

(defn title [label-kw spacing?]
  [view st/section-spacing
   [text {:style      (merge (get-in platform-specific [:component-styles :discovery :subtitle])
                             (when spacing? {:margin-top 16}))
          :uppercase? (get-in platform-specific [:discovery :uppercase-subtitles?])
          :font       :medium}
    (label label-kw)]])

(defview discovery-popular [{:keys [contacts current-account]}]
  [popular-tags [:get-popular-tags 10]]
  [view st/popular-container
   [title :t/popular-tags false]
   (if (pos? (count popular-tags))
     [carousel {:pageStyle st/carousel-page-style
                :gap       0
                :sneak     (if (> (count popular-tags) 1) 16 8)}
      (for [{:keys [name]} popular-tags]
        [discovery-popular-list {:tag             name
                                 :contacts        contacts
                                 :current-account current-account}])]
     [text (label :t/none)])])

(defview discovery-recent [{:keys [current-account]}]
  [discoveries [:get-recent-discoveries]]
  (when (seq discoveries)
    [view st/recent-container
     [title :t/recent true]
     [view st/recent-list
      (let [discoveries (map-indexed vector discoveries)]
        (for [[i {:keys [message-id] :as message}] discoveries]
          ^{:key (str "message-" message-id)}
          [discovery-list-item {:message         message
                                :show-separator? (not= (inc i) (count discoveries))
                                :current-account current-account}]))]]))

(defview discovery []
  [show-search? [:get ::show-search?]
   contacts [:get :contacts]
   current-account [:get-current-account]
   discoveries [:get-recent-discoveries]]
  [view st/discovery-container
   [discovery-toolbar show-search?]
   (if discoveries
     [scroll-view st/scroll-view-container
      [discovery-popular {:contacts        contacts
                          :current-account current-account}]
      [discovery-recent {:current-account current-account}]]
     [view contacts-styles/empty-contact-groups
      ;; todo change icon
      [icon :group_big contacts-styles/empty-contacts-icon]
      [text {:style contacts-styles/empty-contacts-text}
       (label :t/no-statuses-discovered)]])
   [bottom-gradient]])
