(ns status-im.discover.screen
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
    [status-im.discover.styles :as st]
    [status-im.i18n :refer [label]]
    [status-im.components.carousel.carousel :refer [carousel]]
    [status-im.discover.views.popular-list :refer [discover-popular-list]]
    [status-im.discover.views.discover-list-item :refer [discover-list-item]]
    [status-im.contacts.styles :as contacts-styles]
    [status-im.utils.platform :refer [platform-specific]]
    [reagent.core :as r]))

(defn get-hashtags [status]
  (let [hashtags (map #(str/lower-case (str/replace % #"#" "")) (re-seq #"[^ !?,;:.]+" status))]
    (or hashtags [])))

(defn title-content [show-search?]
  [view st/discover-toolbar-content
   (if show-search?
     [text-input {:style             st/discover-search-input
                  :auto-focus        true
                  :placeholder       (label :t/search-tags)
                  :on-blur           (fn [e]
                                       (dispatch [:set :discover-show-search? false]))
                  :on-submit-editing (fn [e]
                                       (let [search   (aget e "nativeEvent" "text")
                                             hashtags (get-hashtags search)]
                                         (dispatch [:set :discover-search-tags hashtags])
                                         (dispatch [:navigate-to :discover-search-results])))}]
     [view
      [text {:style st/discover-title
             :font  :toolbar-title}
       (label :t/discover)]])])

(defn toogle-search [current-value]
  (dispatch [:set :discover-show-search? (not current-value)]))

(defn discover-toolbar [show-search?]
  [toolbar
   {:style          st/discover-toolbar
    :nav-action     {:image   {:source {:uri :icon_hamburger}
                               :style  st/hamburger-icon}
                     :handler open-drawer}
    :custom-content [title-content show-search?]
    :actions        [{:image   {:source {:uri :icon_search}
                                :style  st/search-icon}
                      :handler #(toogle-search show-search?)}]}])

(defn title [label-kw spacing?]
  [view st/section-spacing
   [text {:style      (merge (get-in platform-specific [:component-styles :discover :subtitle])
                             (when spacing? {:margin-top 16}))
          :uppercase? (get-in platform-specific [:discover :uppercase-subtitles?])
          :font       :medium}
    (label label-kw)]])

(defview discover-popular [{:keys [contacts current-account]}]
  [popular-tags [:get-popular-tags 10]]
  [view st/popular-container
   [title :t/popular-tags false]
   (if (pos? (count popular-tags))
     [carousel {:pageStyle st/carousel-page-style
                :gap       0
                :sneak     (if (> (count popular-tags) 1) 16 8)}
      (for [{:keys [name]} popular-tags]
        [discover-popular-list {:tag             name
                                :contacts        contacts
                                :current-account current-account}])]
     [text (label :t/none)])])

(defview discover-recent [{:keys [current-account]}]
  [discoveries [:get-recent-discoveries]]
  (when (seq discoveries)
    [view st/recent-container
     [title :t/recent true]
     [view st/recent-list
      (let [discoveries (map-indexed vector discoveries)]
        (for [[i {:keys [message-id] :as message}] discoveries]
          ^{:key (str "message-recent-" message-id)}
          [discover-list-item {:message         message
                               :show-separator? (not= (inc i) (count discoveries))
                               :current-account current-account}]))]]))

(defview discover [current-view?]
  [show-search? [:get :discover-show-search?]
   contacts [:get :contacts]
   current-account [:get-current-account]
   discoveries [:get-recent-discoveries]]
  [view st/discover-container
   [discover-toolbar (and current-view? show-search?)]
   (if discoveries
     [scroll-view st/scroll-view-container
      [discover-popular {:contacts        contacts
                         :current-account current-account}]
      [discover-recent {:current-account current-account}]]
     [view contacts-styles/empty-contact-groups
      ;; todo change icon
      [icon :group_big contacts-styles/empty-contacts-icon]
      [text {:style contacts-styles/empty-contacts-text}
       (label :t/no-statuses-discovered)]])])
