(ns status-im2.contexts.communities.overview.view
  (:require [i18n.i18n :as i18n]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [utils.re-frame :as rf]
            [quo2.components.community.style :as styles]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.communities.home.actions.view :as home.actions]
            [status-im2.contexts.communities.requests.actions.view :as requests.actions]
            [status-im2.contexts.communities.overview.style :as style]

            ;; TODO move to status-im2 when reimplemented
            [status-im.ui.screens.communities.icon :as communities.icon]))

;; Mocked list items
(def user-list
  [{:full-name "Alicia K"}
   {:full-name "Marcus C"}
   {:full-name "MNO PQR"}
   {:full-name "STU VWX"}])

(defn preview-user-list []
  [rn/view style/preview-user
   [quo/preview-list {:type               :user
                      :more-than-99-label (i18n/label :counter-99-plus)
                      :user               user-list :list-size 4 :size 24}]
   [quo/text {:accessibility-label :communities-screen-title
              :style               {:margin-left 8}
              :size                :label}
    "Join Alicia, Marcus and 2 more"]])                     ;; TODO remove mocked data and use from contacts list/communities members

(def list-of-channels {:Welcome [{:name  "welcome"
                                  :emoji "🤝"}
                                 {:name  "onboarding"
                                  :emoji "🍑"}
                                 {:name  "intro"
                                  :emoji "🦄"}]
                       :General [{:name  "general"
                                  :emoji "🐷"}
                                 {:name  "people-ops"
                                  :emoji "🌏"}
                                 {:name  "announcements"
                                  :emoji "🎺"}]
                       :Mobile  [{:name  "mobile"
                                  :emoji "👽"}]})

(defn channel-list-component []
  [rn/scroll-view {:style {:margin-top 20}}
   [:<>
    (map (fn [category]
           ^{:key (get category 0)}
           [rn/view {:flex 1}
            [quo/divider-label
             {:label            (first category)
              :chevron-position :left}]
            [rn/view
             {:margin-left   8
              :margin-top    10
              :margin-bottom 8}
             [rn/flat-list
              {:shows-horizontal-scroll-indicator false
               :separator                         [rn/view {:margin-top 4}]
               :data                              ((first category) list-of-channels)
               :render-fn                         quo/channel-list-item}]]])
         list-of-channels)]])

(defn overview []
  (let [community-mock (rf/sub [:get-screen-params :community-overview]) ;;TODO stop using mock data and only pass community id
        community (rf/sub [:communities/community (:id community-mock)])
        {:keys [name description locked joined
                status tokens cover tags community-color]} (merge community-mock {:joined (:joined community)})
        icon-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)]
    [rn/view {:flex 1 :border-radius 20}
     [rn/view (styles/community-cover-container 148)        ;; TODO why component's style is used here ?
      [rn/image
       {:source cover
        :style  {:position :absolute
                 :flex     1}}]
      [rn/view {:style {:margin-top 26}}
       [quo/page-nav
        {:horizontal-description? true
         :one-icon-align-left?    true
         :align-mid?              false
         :page-nav-color          :transparent
         :page-nav-background-uri ""
         :mid-section             {:type :text-with-description}
         :right-section-buttons   [{:icon             :i/search
                                    :background-color icon-color}
                                   {:icon             :i/options
                                    :background-color icon-color
                                    :on-press         #(rf/dispatch [:bottom-sheet/show-sheet
                                                                     {:content        (constantly [home.actions/actions community])
                                                                      :content-height 400}])}]
         :left-section            {:icon                  :i/close
                                   :icon-background-color icon-color
                                   :on-press              #(rf/dispatch [:navigate-back])}}]]]
     [rn/view (style/container1)
      [rn/view {:padding-horizontal 20}
       [rn/view (style/container2)
        [communities.icon/community-icon-redesign community 80]]
       (when (and (not joined)
                  (= status :gated))
         [rn/view (styles/permission-tag-styles)
          [quo/permission-tag-container
           {:locked locked
            :status status
            :tokens tokens}]])
       (when joined
         [rn/view {:position :absolute :top 12 :right 12}
          [quo/status-tag {:status {:type :positive} :label (i18n/label :joined)}]])
       [quo/community-title
        {:title       name
         :size        :large
         :description description}]
       [rn/view {:margin-top 12}
        [quo/community-stats-column :card-view]]
       [rn/view {:margin-top 16}
        [quo/community-tags tags]]
       [preview-user-list]
       (when (not joined)
         ;; TODO (flexsurfer) we shouldn't have custom buttons, this should be a component
         [quo/button
          {:on-press                  #(rf/dispatch [:bottom-sheet/show-sheet
                                                     {:content        (constantly [requests.actions/actions community])
                                                      :content-height 300}])
           :override-background-color community-color
           :style
           {:width        "100%"
            :margin-top   20
            :margin-left  :auto
            :margin-right :auto}
           :before                    :i/communities}
          (i18n/label :join-open-community)])]
      [channel-list-component]]]))
