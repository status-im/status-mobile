(ns syng-im.components.discovery.discovery

  (:require
    [syng-im.utils.debug :refer [log]]
    [re-frame.core :refer [dispatch]]
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
                                             (realm/write (fn []
                                                            (let [number (rand-int 30)]
                                                            (realm/create :discoveries
                                                                          {:name         (str "c" number)
                                                                           :status       (str "Status " number)
                                                                           :whisper-id   (str number)
                                                                           :photo        ""
                                                                           :location     ""
                                                                           :tags         [{:name "tag1"} {:name "tag2"}]
                                                                           :last-updated (new js/Date)} true)
                                                            (dispatch [:updated-discoveries])))))
                         ;; temporary dispatch for testing
                         :onActionSelected (fn [index]
                                             (if @showSearch
                                               (reset! showSearch false)
                                               (reset! showSearch true)))}
        (if @showSearch
          [text-input {:underlineColorAndroid "transparent"
                       :value                 (:search @search-input)
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
                                                  (log (str new-text "-" old-text))
                                                  (if (not (= new-text old-text))
                                                    (swap! search-input assoc :search new-text))
                                                  ))
                       :onSubmitEditing       (fn [e]
                                                (log (aget e "nativeEvent" "text")))}]
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
