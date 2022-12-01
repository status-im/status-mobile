(ns status-im2.contexts.communities.overview.view
  (:require
   [i18n.i18n :as i18n]
   [react-native.core :as rn]
   [quo2.core :as quo]
   [utils.re-frame :as rf]
   [quo2.foundations.colors :as colors]
   [status-im2.contexts.communities.overview.style :as style]
   ;; TODO move to status-im2 when reimplemented
   [status-im.ui.screens.communities.icon :as communities.icon]
   [oops.core :as oops]
   [reagent.core :as reagent]
   [quo.platform :as platform]
   [status-im2.contexts.communities.requests.actions.view :as requests.actions]
   [status-im2.contexts.communities.home.actions.view :as home.actions]
   [quo2.components.community.token-gating :as token-gating]
   [status-im.constants :as constants]
   [status-im.react-native.resources :as resources]
   [status-im.utils.utils :as utils]))

(def knc-token-img (js/require "../resources/images/tokens/mainnet/KNC.png"))
(def mana-token-img (js/require "../resources/images/tokens/mainnet/MANA.png"))
(def rare-token-img (js/require "../resources/images/tokens/mainnet/RARE.png"))
(def eth-token-img (js/require "../resources/images/tokens/mainnet/ETH.png"))
(def dai-token-img (js/require "../resources/images/tokens/mainnet/DAI.png"))

;; Mocked list items
(def user-list
  [{:full-name "Alicia K"}
   {:full-name "Marcus C"}
   {:full-name "MNO PQR"}
   {:full-name "STU VWX"}])

(defn preview-user-list []
  [rn/view style/preview-user
   [quo/preview-list {:type :user
                      :user user-list :list-size 4 :size 24}]
   [quo/text {:accessibility-label :communities-screen-title
              :style {:margin-left 8}
              :size                :label}
    "Join Alicia, Marcus and 2 more"]]) ;; TODO remove mocked data and use from contacts list/communities members

(defn open-token-gating-mocked [name emoji channel-color]
  #(rf/dispatch
    [:bottom-sheet/show-sheet
     {:content
      (constantly [token-gating/token-gating
                   {:channel {:name name
                              :community-color (colors/custom-color :pink 50)
                              :emoji emoji
                              :emoji-background-color channel-color
                              :on-enter-channel (fn [] (utils/show-popup "Entered channel" "Wuhuu!! You successfully entered the channel :)"))
                              :gates {:read [{:token "KNC"
                                              :token-img-src knc-token-img
                                              :amount 200
                                              :is-sufficient? true}
                                             {:token "MANA"
                                              :token-img-src mana-token-img
                                              :amount 10
                                              :is-sufficient? false
                                              :is-purchasable true}
                                             {:token "RARE"
                                              :token-img-src rare-token-img
                                              :amount 10
                                              :is-sufficient? false}]
                                      :write [{:token "KNC"
                                               :token-img-src knc-token-img
                                               :amount 200
                                               :is-sufficient? true}
                                              {:token "DAI"
                                               :token-img-src dai-token-img
                                               :amount 20
                                               :is-purchasable true
                                               :is-sufficient? false}
                                              {:token "ETH"
                                               :token-img-src eth-token-img
                                               :amount 0.5
                                               :is-sufficient? false}]}}}])
      :content-height 210}]))

(def list-of-channels {:Welcome [{:name "welcome"
                                  :emoji "🤝"}
                                 {:name  "onboarding"
                                  :emoji "🍑"
                                  :locked? true
                                  :on-press #((open-token-gating-mocked
                                               "onboarding"
                                               "🍑"
                                               (colors/custom-color :pink 50)))}
                                 {:name "intro"
                                  :emoji "🦄"
                                  :locked? true
                                  :on-press #((open-token-gating-mocked
                                               "intro"
                                               "🦄"
                                               (colors/custom-color :pink 50)))}]
                       :General [{:name  "general"
                                  :emoji "🐷"}
                                 {:name  "people-ops"
                                  :emoji "🌏"
                                  :locked? true
                                  :on-press #((open-token-gating-mocked
                                               "onboarding"
                                               "🌏"
                                               (colors/custom-color :blue 50)))}
                                 {:name "announcements"
                                  :emoji "🎺"}]
                       :Mobile [{:name "mobile"
                                 :emoji "👽"}
                                {:name "mobile-ui"
                                 :emoji "👽"}
                                {:name "mobile-ui-reviews"
                                 :emoji "👽"}]
                       :Desktop [{:name "desktop"
                                  :emoji "👽"}
                                 {:name "desktop-ui"
                                  :emoji "👽"}
                                 {:name "desktop-ui-reviews"
                                  :emoji "👽"}
                                 {:name "desktop2"
                                  :emoji "👽"}
                                 {:name "desktop-ui2"
                                  :emoji "👽"}
                                 {:name "desktop-ui2-reviews"
                                  :emoji "👽"}]})

(defn channel-list-component [channel-heights first-channel-height]
  [rn/view {:on-layout #(swap! first-channel-height
                               (fn [] (+  (if platform/ios? 0 38) (int (Math/ceil (oops/oget % "nativeEvent.layout.y"))))))
            :style {:margin-top 20 :flex 1}}
   (map-indexed (fn [index category]
                  (let [first-category (first category)]
                    ^{:key first-category}
                    [rn/view
                     {:flex 1
                      :key (str index first-category)
                      :on-layout #(swap! channel-heights
                                         (fn []
                                           (sort-by :height
                                                    (conj @channel-heights
                                                          {:height (int (oops/oget % "nativeEvent.layout.y"))
                                                           :label first-category}))))}

                     [quo/divider-label
                      {:label first-category
                       :chevron-position :left}]
                     [rn/view
                      {:margin-left   8
                       :margin-top    10
                       :margin-bottom 8}
                      (map-indexed (fn [inner-index channel-data] [rn/view {:key (str inner-index (:name channel-data)) :margin-top 4}
                                                                   [quo/channel-list-item channel-data]]) (first-category list-of-channels))]]))
                list-of-channels)])

(defn icon-color []
  (colors/theme-colors
   colors/white-opa-40
   colors/neutral-80-opa-40))

(defn get-platform-value [value] (if platform/ios? (+ value 44) value))

(def scroll-0 (if platform/ios? -44 0))
(def scroll0 (if platform/ios? 44 0))
(def scroll1 (if platform/ios? 86 134))

(def scroll2 (if platform/ios? -26 18))

(def max-image-size 80)
(def min-image-size 32)

(defn diff-with-max-min [value maximum minimum]
  (->>
   (+ value scroll0)
   (- maximum)
   (max minimum)
   (min maximum)))

(defn get-header-size [scroll-height]
  (if (<= scroll-height scroll2)
    0
    (->>
     (+ (get-platform-value -17) scroll-height)
     (* (if platform/ios? 3 1))
     (max 0)
     (min (if platform/ios? 100 124)))))

(defn community-card-page-view [{:keys [name id description locked joined
                                        status tokens cover images tags community-color] :as community}]
  (let [community-icon (memoize (fn [] [communities.icon/community-icon-redesign community 24]))
        thumbnail-image (get-in images [:thumbnail])
        scroll-height (reagent/atom scroll-0)
        channel-heights (reagent/atom [])
        first-channel-height (reagent/atom 0)]

    (fn []
      [:<>
       [:<>
        [rn/image
         {:source      cover
          :position :absolute
          :style  (style/image-slider (get-header-size @scroll-height))}]
        [rn/blur-view (style/blur-slider (get-header-size @scroll-height))]]
       [rn/view {:style {:z-index 6 :margin-top (if platform/ios? 56 12)}}
        [quo/page-nav
         {:horizontal-description?            true
          :one-icon-align-left?               true
          :align-mid?                         false
          :page-nav-color                     :transparent
          :page-nav-background-uri            ""
          :mid-section {:type  :text-with-description
                        :main-text (when (>= @scroll-height scroll1) name)
                        :description-img (when (>= @scroll-height scroll1) community-icon)}
          :right-section-buttons [{:icon :i/search
                                   :background-color (icon-color)}
                                  {:icon :i/options
                                   :background-color (icon-color)
                                   :on-press #(rf/dispatch [:bottom-sheet/show-sheet
                                                            {:content (constantly [home.actions/actions community])
                                                             :content-height 400}])}]
          :left-section {:icon                  :i/close
                         :icon-background-color (icon-color)
                         :on-press #(rf/dispatch [:navigate-back])}}]
        (when (>= @scroll-height @first-channel-height)
          [rn/blur-view style/blur-channel-header
           [quo/divider-label
            {:label (:label (last (filter (fn [{:keys [height]}]
                                            (>= @scroll-height (+ height @first-channel-height)))
                                          @channel-heights)))
             :chevron-position :left}]])]
       [rn/scroll-view {:style (style/scroll-view-container (diff-with-max-min @scroll-height 16 0))
                        :shows-vertical-scroll-indicator false
                        :scroll-event-throttle 1
                        :on-scroll #(swap! scroll-height (fn [] (int (oops/oget % "nativeEvent.contentOffset.y"))))}
        [rn/view {:style {:height 151}}
         [rn/image
          {:source      cover
           :style  {:overflow :visible
                    :flex 1}}]]
        [rn/view {:flex 1
                  :border-radius (diff-with-max-min @scroll-height 16 0)
                  :background-color (colors/theme-colors
                                     colors/white
                                     colors/neutral-90)}
         [rn/view
          [rn/view {:padding-horizontal 20}
           [rn/view {:border-radius    40
                     :border-width     1
                     :border-color     colors/white
                     :position         :absolute
                     :top              (if (<= @scroll-height scroll-0)
                                         -40
                                         (->> (+ scroll0 @scroll-height)
                                              (* (if platform/ios? 3 1))
                                              (+ -40)
                                              (min 8)))

                     :left             17
                     :padding          2
                     :background-color (colors/theme-colors
                                        colors/white
                                        colors/neutral-90)}
            [communities.icon/community-icon-redesign community
             (->> (+ scroll0 @scroll-height)
                  (* (if platform/ios? 3 1))
                  (- max-image-size)
                  (max  min-image-size)
                  (min max-image-size))]]
           (when (and (not joined)
                      (= status :gated))
             [rn/view {:position         :absolute
                       :top              8
                       :right            8}
              [quo/permission-tag-container
               {:locked       locked
                :status       status
                :tokens       tokens
                :on-press     #(rf/dispatch
                                [:bottom-sheet/show-sheet
                                 {:content
                                  (constantly [token-gating/token-gating
                                               {:community {:name name
                                                            :community-color colors/primary-50
                                                            :community-avatar (cond
                                                                                (= id constants/status-community-id)
                                                                                (resources/get-image :status-logo)
                                                                                (seq thumbnail-image)
                                                                                thumbnail-image)
                                                            :gates {:join [{:token "KNC"
                                                                            :token-img-src knc-token-img
                                                                            :amount 200
                                                                            :is-sufficient? true}
                                                                           {:token "MANA"
                                                                            :token-img-src mana-token-img
                                                                            :amount 10
                                                                            :is-sufficient? false
                                                                            :is-purchasable true}
                                                                           {:token "RARE"
                                                                            :token-img-src rare-token-img
                                                                            :amount 10
                                                                            :is-sufficient? false}]}}}])
                                  :content-height 210}])}]])

           (when joined
             [rn/view {:position         :absolute
                       :top              12
                       :right            12}
              [quo/status-tag {:status {:type :positive} :label (i18n/label :joined)}]])
           [rn/view  {:margin-top  56}
            [quo/text
             {:accessibility-label :chat-name-text
              :number-of-lines     1
              :ellipsize-mode      :tail
              :weight              :semi-bold
              :size                :heading-1} name]]

           [quo/text
            {:accessibility-label :community-description-text
             :number-of-lines     2
             :ellipsize-mode      :tail
             :weight  :regular
             :size    :paragraph-1
             :style {:margin-top 8 :margin-bottom 12}}
            description]
           [quo/community-stats-column :card-view]
           [rn/view {:margin-top 12}]
           [quo/community-tags tags]
           [preview-user-list]
           (when-not joined
             [quo/button
              {:on-press  #(rf/dispatch [:bottom-sheet/show-sheet
                                         {:content (constantly [requests.actions/request-to-join community])
                                          :content-height 300}])
               :override-background-color community-color
               :style
               {:width "100%"
                :margin-top 20
                :margin-left :auto
                :margin-right :auto}
               :before :i/communities}
              (i18n/label :join-open-community)])]
          [channel-list-component channel-heights first-channel-height]]]]])))

(defn overview []
  (let [community-mock (rf/sub [:get-screen-params :community-overview]) ;;TODO stop using mock data and only pass community id
        community (rf/sub [:communities/community (:id community-mock)])]

    [rn/view {:style
              {:position :absolute
               :top (if platform/ios? 0 44)
               :width "100%"
               :height "110%"}}
     [community-card-page-view
      (merge community-mock {:joined (:joined community)})]]))

