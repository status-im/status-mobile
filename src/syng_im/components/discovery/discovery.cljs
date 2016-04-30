(ns syng-im.components.discovery.discovery

  (:require
    [syng-im.utils.logging :as log]
    [re-frame.core :refer [dispatch]]
    [syng-im.models.discoveries :refer [save-discoveries]]
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      text
                                      text-input
                                      toolbar-android]]
    [reagent.core :as r]
    [syng-im.components.discovery.discovery-popular :refer [discovery-popular]]
    [syng-im.components.discovery.discovery-recent :refer [discovery-recent]]
    [syng-im.resources :as res]
    [syng-im.persistence.realm :as realm]))

(def search-input (atom {:search "x"}))

(defn get-hashtags [status]
  (let [hashtags (map #(subs % 1) (re-seq #"#[^ !?,;:.]+" status))]
    (if hashtags
      hashtags
      [])))

(defn discovery [{:keys [navigator]}]
  (let [showSearch (r/atom false)]
    (fn []
      [view {:style {:flex            1
                     :backgroundColor "#eef2f5"}}
       [toolbar-android {:titleColor       "#4A5258"
                         :navIcon          res/menu
                         :actions          [{:title "Search"
                                             :icon  res/search
                                             :show  "always"}]
                         :style            {:backgroundColor "#eef2f5"
                                            :justifyContent  "center"
                                            :height          56
                                            :elevation       0}
                         :onIconClicked    (fn []
                                             (let [number (rand-int 999)]
                                              (do
                                                (save-discoveries [{:name         (str "Name " number)
                                                                    :status       (str "Status " number)
                                                                    :whisper-id   (str number)
                                                                    :photo        ""
                                                                    :location     ""
                                                                    :tags         ["tag1" "tag2" "tag3"]
                                                                    :last-updated (new js/Date)}])
                                                (dispatch [:updated-discoveries]))))
                         ;; temporary dispatch for testing
                         :onActionSelected (fn [index]
                                             (if @showSearch
                                               (reset! showSearch false)
                                               (reset! showSearch true)))}
        (if @showSearch
          [text-input {:underlineColorAndroid "transparent"
                       ;:value                 (:search @search-input)
                       :style                 {:flex       1
                                               :marginLeft 18
                                               :lineHeight 42
                                               :fontSize   14
                                               :fontFamily "Avenir-Roman"
                                               :color      "#9CBFC0"}
                       :autoFocus             true
                       :placeholder           "Type your search tags here"
                       :onChangeText          (fn [new-text]
                                                (let [old-text (:search @search-input)]
                                                  (log/debug (str new-text "-" old-text))))
                       :onSubmitEditing       (fn [e]
                                                (let [search (aget e "nativeEvent" "text")
                                                      hashtags (get-hashtags search)]
                                                  (dispatch [:broadcast-status search hashtags])))}]
          [text "Discover"])]

       [scroll-view {:style {}}
        [view {:style {:paddingTop 5}}
         [text {:style {:color      "#b2bdc5"
                        :fontSize   14
                        :textAlign "center"}} "Discover popular contacts \n around the world"]]
        [view {:style {:paddingLeft   30
                       :paddingTop    15
                       :paddingBottom 15}}
         [text {:style {:color      "#b2bdc5"
                        :fontSize   14
                        :fontWeight "bold"}} "Popular Tags"]]
        [discovery-popular]
        [view {:style {:paddingLeft   30
                       :paddingTop    15
                       :paddingBottom 15}}
         [text {:style {:color      "#b2bdc5"
                        :fontSize   14
                        :fontWeight "bold"}} "Recent"]]
        [discovery-recent]
        ]
       ]
      )
  ))
  (comment
    (def page-width (aget (natal-shell.dimensions/get "window") "width"))
    (def page-height (aget (natal-shell.dimensions/get "window") "height"))
    )
