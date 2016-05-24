(ns status-im.discovery.screen
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [dispatch subscribe]]
    [status-im.components.react :refer [view
                                        scroll-view
                                        text
                                        text-input]]
    [status-im.components.toolbar :refer [toolbar]]
    [status-im.discovery.views.popular :refer [popular]]
    [status-im.discovery.views.recent :refer [discovery-recent]]
    [status-im.discovery.styles :as st]))

(defn get-hashtags [status]
  (let [hashtags (map #(subs % 1) (re-seq #"#[^ !?,;:.]+" status))]
    (or hashtags [])))

(defn title-content [show-search]
  [view st/discovery-toolbar-content
   (if show-search
     [text-input {:style           st/discovery-search-input
                  :autoFocus       true
                  :placeholder     "Type your search tags here"
                  :onSubmitEditing (fn [e]
                                     (let [search (aget e "nativeEvent" "text")
                                           hashtags (get-hashtags search)]
                                       (dispatch [:broadcast-status search hashtags])))}]
     [view
      [text {:style st/discovery-title} "Discover"]])])

(defn toogle-search [current-value]
  (dispatch [:set ::show-search (not current-value)]))

(defview discovery []
  [show-search [:get ::show-search]]
  [view st/discovery-container
   [toolbar
    {:style          st/discovery-toolbar
     :nav-action     {:image   {:source {:uri :icon_hamburger}
                                :style  st/hamburger-icon}
                      :handler #(dispatch [:create-fake-discovery!])}
     :custom-content [title-content show-search]
     :action         {:image   {:source {:uri :icon_search}
                                :style  st/search-icon}
                      :handler #(toogle-search show-search)}}]
   [scroll-view st/scroll-view-container
    [view st/section-spacing
     [text {:style st/discovery-subtitle} "Popular tags"]]
    [popular]
    [view st/section-spacing
     [text {:style st/discovery-subtitle} "Recent"]]
    [discovery-recent]]])
