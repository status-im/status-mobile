(ns syng-im.components.discovery.discovery

  (:require
    [re-frame.core :refer [subscribe dispatch dispatch-sync]]
    [syng-im.components.react :refer [android?
                                      view
                                      scroll-view
                                      text
                                      text-input
                                      image
                                      navigator
                                      toolbar-android]]
    [reagent.core :as r]
    [syng-im.components.discovery.discovery-popular :refer [discovery-popular]]
    [syng-im.components.discovery.discovery-recent :refer [discovery-recent]]
    [syng-im.models.discoveries :refer [generate-discoveries
                                        generate-discovery
                                        save-discoveries]]
    [syng-im.utils.listview :refer [to-realm-datasource]]
    [syng-im.resources :as res]
    [syng-im.persistence.realm :as realm]))

(def log (.-log js/console))

(def search-input (atom {:search "x"}))

(def toolbar-title [text "Discover"])
(def toolbar-search [text-input {:underlineColorAndroid "transparent"
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
                                                          (log (aget e "nativeEvent" "text")))}])

(def showSearch (r/atom false))

(def content (r/atom toolbar-title))

(defn toggle-search []
  (if @showSearch
    (do
      (reset! showSearch false)
      (reset! content toolbar-title))
    (do
      (reset! showSearch true)
      (reset! content toolbar-search))))

(defn discovery [{:keys [navigator]}]
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
                                            :elevation       2}
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
                         :onActionSelected (fn [index]
                                             (toggle-search))}
        @content]

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
