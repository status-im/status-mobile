(ns syng-im.components.discovery.discovery

  (:require
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      text
                                      image
                                      navigator
                                      toolbar-android]]
    [reagent.core :as r]
    [syng-im.components.discovery.discovery-popular :refer [discovery-popular]]
    [syng-im.components.discovery.discovery-recent :refer [discovery-recent]]
    [syng-im.models.discoveries :refer [generate-discoveries
                                        get-discovery-popular
                                        get-discovery-recent]]
    [syng-im.resources :as res]))

(defn discovery [{:keys [navigator]}]
  (let [discoveries (subscribe [:get-discoveries])
        pop-discoveries (get-discovery-popular 3)]
    (fn []
      [view {:style {:flex            1
                     :backgroundColor "#edf2f5"}}
       [toolbar-android {:title            "Discover"
                         :titleColor       "#4A5258"
                         :navIcon          res/menu
                         :actions          [{:title "Search"
                                             :icon  res/search
                                             :show  "always"}]
                         :style            {:backgroundColor "white"
                                            :justifyContent "center"
                                            :height          56
                                            :elevation       2}
                         :onIconClicked    (fn []
                                             (.log console "testttt"))
                         :onActionSelected (fn [index]
                                             (index))}]

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
        [discovery-popular pop-discoveries]
        [view {:style {:paddingLeft   30
                       :paddingTop    15
                       :paddingBottom 15}}
         [text {:style {:color      "#b2bdc5"
                        :fontSize   14
                        :fontWeight "bold"}} "Recent"]]
        [discovery-recent (get-discovery-recent 10)]
        ]
       ]
      )
    )
  )
  (comment
    (def page-width (aget (natal-shell.dimensions/get "window") "width"))
    (def page-height (aget (natal-shell.dimensions/get "window") "height"))
    [view {:style {:flex            1
                   :backgroundColor "white"}}
     [toolbar-android {:title            "Discover"
                       :titleColor       "#4A5258"
                       :navIcon          res/menu
                       :actions          [{:title "Search"
                                           :icon  res/search
                                           :show  "always"}]
                       :style            {:backgroundColor "white"
                                          :height          56
                                          :elevation       2}
                       :onIconClicked    (fn []
                                           (.log console "testttt"))
                       :onActionSelected (fn [index]
                                           (index))}]
     [scroll-view {:style { }}
      [view {:style {:paddingLeft 30
                     :paddingTop 15
                     :paddingBottom 15}}
       [text {:style {:color "#232323"
                      :fontSize   18
                      :fontWeight "bold"}} "Popular Tags"]]
      [carousel {:pageStyle {:backgroundColor "white", :borderRadius 5}
                 :pageWidth (- page-width 60)}
       [view {:style {:height (- page-height 100)}} [text {:style {:color "#232323"
                                                                   :fontSize   18
                                                                   :fontWeight "bold"}} "Popular Tags"]]
       [view [text "Welcome to Syng"]]
       [view [text "Welcome to Syng"]]
       [view [text "Welcome to Syng"]]]
      [view {:style {:paddingLeft 30
                     :paddingTop 15
                     :paddingBottom 15}}
       [text {:style {:color "#232323"
                      :fontSize   18
                      :fontWeight "bold"}} "Recent"]]
      [view {:style {:height 200}}]
      ]]
    )
