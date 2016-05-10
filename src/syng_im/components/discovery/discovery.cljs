(ns syng-im.components.discovery.discovery

  (:require
    [syng-im.utils.logging :as log]
    [re-frame.core :refer [dispatch]]
    [syng-im.models.discoveries :refer [save-discoveries]]
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      text
                                      text-input]]
    [reagent.core :as r]
    [syng-im.components.toolbar :refer [toolbar]]
    [syng-im.components.discovery.discovery-popular :refer [discovery-popular]]
    [syng-im.components.discovery.discovery-recent :refer [discovery-recent]]
    [syng-im.components.discovery.styles :as st]
    [syng-im.persistence.realm :as realm]))

(def search-input (atom {:search "x"}))

(defn get-hashtags [status]
  (let [hashtags (map #(subs % 1) (re-seq #"#[^ !?,;:.]+" status))]
    (if hashtags
      hashtags
      [])))

(defn title-content [showSearch]
  (if showSearch
    [text-input {:underlineColorAndroid "transparent"
                 :style                 st/discovery-search-input
                 :autoFocus             true
                 :placeholder           "Type your search tags here"
                 :onSubmitEditing       (fn [e]
                                          (let [search (aget e "nativeEvent" "text")
                                                hashtags (get-hashtags search)]
                                            (dispatch [:broadcast-status search hashtags])))}]
    [view
     [text {:style st/discovery-title} "Discover"]]))

(defn create-fake-discovery []
  (let [number (rand-int 999)]
    (do
      (save-discoveries [{:name         (str "Name " number)
                          :status       (str "Status This is some longer status to get the second line " number)
                          :whisper-id   (str number)
                          :photo        ""
                          :location     ""
                          :tags         ["tag1" "tag2" "tag3"]
                          :last-updated (new js/Date)}])
      (dispatch [:updated-discoveries]))))

(defn discovery [{:keys [navigator]}]
  (let [showSearch (r/atom false)]
    (fn []
      [view {:style {:flex            1
                     :backgroundColor "#eef2f5"}}
       [toolbar {:style st/discovery-toolbar
                 :navigator navigator
                 :nav-action {:image {:source {:uri "icon_hamburger"}
                                      :style  {:width      16
                                               :height     12}}
                              :handler create-fake-discovery}
                 :title     "Add Participants"
                 :content   (title-content @showSearch)
                 :action    {:image {:source {:uri "icon_search"}
                                     :style  {:width  17
                                              :height 17}}
                             :handler (fn []
                                        (if @showSearch
                                          (reset! showSearch false)
                                          (reset! showSearch true)))}}]
       [scroll-view {:style {}}
        [view {:style st/section-spacing}
         [text {:style st/discovery-subtitle} "Popular tags"]]
        [discovery-popular navigator]
        [view {:style st/section-spacing}
         [text {:style st/discovery-subtitle} "Recent"]]
        [discovery-recent]]])))
  (comment
    (def page-width (aget (natal-shell.dimensions/get "window") "width"))
    (def page-height (aget (natal-shell.dimensions/get "window") "height"))
    )
