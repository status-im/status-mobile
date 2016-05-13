(ns syng-im.discovery.screen

  (:require
    [syng-im.utils.logging :as log]
    [re-frame.core :refer [dispatch subscribe]]
    [syng-im.models.discoveries :refer [save-discoveries]]
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      text
                                      text-input]]
    [reagent.core :as r]
    [syng-im.components.toolbar :refer [toolbar]]
    [syng-im.discovery.views.popular :refer [popular]]
    [syng-im.discovery.discovery-recent :refer [discovery-recent]]
    [syng-im.discovery.styles :as st]
    [syng-im.persistence.realm :as realm]))

(defn get-hashtags [status]
  (let [hashtags (map #(subs % 1) (re-seq #"#[^ !?,;:.]+" status))]
    (or hashtags [])))

(defn title-content [showSearch]
  (if showSearch
    [text-input {:style           st/discovery-search-input
                 :autoFocus       true
                 :placeholder     "Type your search tags here"
                 :onSubmitEditing (fn [e]
                                    (let [search   (aget e "nativeEvent" "text")
                                          hashtags (get-hashtags search)]
                                      (dispatch [:broadcast-status search hashtags])))}]
    [view
     [text {:style st/discovery-title} "Discover"]]))

(defn toogle-search [current-value]
  (dispatch [:set ::show-search (not current-value)]))

(defn discovery []
  []
  (let [show-search (subscribe [:get ::show-search])]
    (fn []
      [view {:flex            1
             :backgroundColor :#eef2f5}
       [toolbar
        {:style      st/discovery-toolbar
         :nav-action {:image   {:source {:uri :icon_hamburger}
                                :style  {:width  16
                                         :height 12}}
                      :handler #(dispatch [:create-fake-discovery!])}
         :title      "Add Participants"
         :content    (title-content @show-search)
         :action     {:image   {:source {:uri :icon_search}
                                :style  {:width  17
                                         :height 17}}
                      :handler #(toogle-search @show-search)}}]
       [scroll-view {:style {}}
          [view st/section-spacing
           [text {:style st/discovery-subtitle} "Popular tags"]]
          [popular]
          [view st/section-spacing
           [text {:style st/discovery-subtitle} "Recent"]]
          [discovery-recent]]])))
