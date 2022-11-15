(ns status-im.ui.screens.communities.community-overview-redesign
  (:require  [status-im.i18n.i18n :as i18n]
             [quo.react-native :as rn]
             [quo2.components.markdown.text :as text]
             [quo2.components.buttons.button :as button]
             [quo2.components.list-items.preview-list :as preview-list]
             [quo2.components.list-items.channel :as channel]
             [quo2.components.dividers.divider-label :as divider-label]
             [quo2.components.community.community-view :as community-view]
             [quo2.components.tags.status-tags :as status-tags]
             [quo2.components.community.style :as styles]
             [quo2.foundations.colors :as colors]
             [quo2.components.navigation.page-nav :as page-nav]
             [status-im.ui.screens.communities.request-to-join-bottom-sheet-redesign :as request-to-join]
             [status-im.ui.screens.communities.community-options-bottom-sheet :as options-menu]
             [status-im.ui.components.list.views :as list]
             [status-im.utils.handlers :refer [<sub >evt]]
             [status-im.ui.screens.communities.icon :as communities.icon]))

;; Mocked list items
(def user-list
  [{:full-name "Alicia K"}
   {:full-name "Marcus C"}
   {:full-name "MNO PQR"}
   {:full-name "STU VWX"}])

(defn preview-user-list []
  [rn/view {:style {:flex-direction :row
                    :align-items :center
                    :margin-top 20}}
   [preview-list/preview-list {:type :user
                               :more-than-99-label (i18n/label :counter-99-plus)
                               :user user-list :list-size 4 :size 24}]
   [text/text {:accessibility-label :communities-screen-title
               :style {:margin-left 8}
               :size                :label}
    "Join Alicia, Marcus and 2 more"]]) ;; TODO remove mocked data and use from contacts list/communities members

(def list-of-channels {:Welcome [{:name "welcome"
                                  :emoji "🤝"}
                                 {:name  "onboarding"
                                  :emoji "🍑"}
                                 {:name "intro"
                                  :emoji "🦄"}]
                       :General [{:name  "general"
                                  :emoji "🐷"}
                                 {:name  "people-ops"
                                  :emoji "🌏"}
                                 {:name "announcements"
                                  :emoji "🎺"}]
                       :Mobile [{:name "mobile"
                                 :emoji "👽"}]})

(defn channel-list-component []
  [rn/scroll-view {:style {:margin-top 20}}
   [:<> {:style {:flex 1}}
    (map (fn [category]
           ^{:key (get category 0)}
           [rn/view {:flex 1}
            [divider-label/divider-label
             {:label (first category)
              :chevron-position :left}]
            [rn/view
             {:margin-left   8
              :margin-top    10
              :margin-bottom 8}
             [list/flat-list
              {:shows-horizontal-scroll-indicator false
               :separator                         [rn/view {:margin-top 4}]
               :data                              ((first category) list-of-channels)
               :render-fn                         channel/list-item}]]])
         list-of-channels)]])

(defn icon-color []
  (colors/theme-colors
   colors/white-opa-40
   colors/neutral-80-opa-40))

(defn community-card-page-view [{:keys [name description locked joined
                                        status tokens cover tags community-color] :as community}]
  [rn/view
   {:style
    {:flex 1
     :border-radius   20}}
   [rn/view (styles/community-cover-container 148)

    [rn/image
     {:source      cover
      :style  {:position :absolute
               :flex 1}}]
    [rn/view {:style {:margin-top 26}}
     [page-nav/page-nav
      {:horizontal-description?            true
       :one-icon-align-left?               true
       :align-mid?                         false
       :page-nav-color                     :transparent
       :page-nav-background-uri            ""
       :mid-section {:type                   :text-with-description}
       :right-section-buttons [{:icon :i/search
                                :background-color (icon-color)}
                               {:icon :i/options
                                :background-color (icon-color)
                                :on-press #(>evt [:bottom-sheet/show-sheet
                                                  {:content (constantly [options-menu/options-menu community])
                                                   :content-height 400}])}]
       :left-section {:icon                  :i/close
                      :icon-background-color (icon-color)
                      :on-press #(>evt [:navigate-back])}}]]]
   [rn/view {:flex               1
             :height 20
             :border-radius      16
             :background-color (colors/theme-colors
                                colors/white
                                colors/neutral-90)}
    [rn/view {:padding-horizontal 20}
     [rn/view {:border-radius    40
               :border-width 1
               :border-color colors/white
               :position         :absolute
               :top              (- (/ 80 2))
               :left             (/ 70 4)
               :padding          2
               :background-color (colors/theme-colors
                                  colors/white
                                  colors/neutral-90)}
      [communities.icon/community-icon-redesign community 80]]
     (when (and (not joined)
                (= status :gated))
       [rn/view (styles/permission-tag-styles)
        [community-view/permission-tag-container
         {:locked       locked
          :status       status
          :tokens       tokens}]])
     (when joined
       [rn/view {:position         :absolute
                 :top              12
                 :right            12}
        [status-tags/status-tag {:status {:type :positive} :label (i18n/label :joined)}]])
     [community-view/community-title
      {:title name
       :size :large
       :description description}]
     [rn/view {:margin-top 12}
      [community-view/community-stats-column :card-view]]
     [rn/view {:margin-top 16}
      [community-view/community-tags tags]]
     [preview-user-list]
     (when (not joined)
       [button/button
        {:on-press  #(>evt [:bottom-sheet/show-sheet
                            {:content (constantly [request-to-join/request-to-join community])
                             :content-height 300}])
         :override-background-color community-color
         :style
         {:width "100%"
          :margin-top 20
          :margin-left :auto
          :margin-right :auto}
         :before :i/communities}
        (i18n/label :join-open-community)])]
    [channel-list-component]]])

(defn overview []
  (let [community-mock (<sub [:get-screen-params :community-overview]) ;;TODO stop using mock data and only pass community id
        community (<sub [:communities/community (:id community-mock)])]
    [rn/view {:style
              {:height "100%"}}

     [community-card-page-view
      (merge community-mock {:joined (:joined community)})]]))

