(ns status-im2.contexts.communities.overview.view
  (:require [utils.i18n :as i18n]
            [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.blur :as blur]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.common.scroll-page.view :as scroll-page]
            [status-im2.contexts.communities.overview.style :as style]
            [status-im2.contexts.communities.menus.community-options.view :as options]
            [status-im2.contexts.communities.menus.request-to-join.view :as join-menu]
            [utils.re-frame :as rf]))

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

(defn preview-user-list
  []
  [rn/view style/preview-user
   [quo/preview-list
    {:type      :user
     :user      user-list
     :list-size 4
     :size      24}]
   [quo/text
    {:accessibility-label :communities-screen-title
     :style               {:margin-left 8}
     :size                :label}
    "Join Alicia, Marcus and 2 more"]]) ;; TODO remove mocked data and use from contacts list/communities members

(defn open-token-gating-mocked
  [name emoji channel-color]
  #(rf/dispatch
    [:bottom-sheet/show-sheet
     {:content
      (fn []
        [quo/token-gating
         {:channel {:name                   name
                    :community-color        (colors/custom-color :pink 50)
                    :emoji                  emoji
                    :emoji-background-color channel-color
                    :on-enter-channel       (fn []
                                              (js/alert
                                               "Entered channel"
                                               "Wuhuu!! You successfully entered the channel :)"))
                    :gates                  {:read  [{:token          "KNC"
                                                      :token-img-src  knc-token-img
                                                      :amount         200
                                                      :is-sufficient? true}
                                                     {:token          "MANA"
                                                      :token-img-src  mana-token-img
                                                      :amount         10
                                                      :is-sufficient? false
                                                      :is-purchasable true}
                                                     {:token          "RARE"
                                                      :token-img-src  rare-token-img
                                                      :amount         10
                                                      :is-sufficient? false}]
                                             :write [{:token          "KNC"
                                                      :token-img-src  knc-token-img
                                                      :amount         200
                                                      :is-sufficient? true}
                                                     {:token          "DAI"
                                                      :token-img-src  dai-token-img
                                                      :amount         20
                                                      :is-purchasable true
                                                      :is-sufficient? false}
                                                     {:token          "ETH"
                                                      :token-img-src  eth-token-img
                                                      :amount         0.5
                                                      :is-sufficient? false}]}}}])
      :content-height 210}]))

(def mock-list-of-channels
  {:Welcome [{:name  "welcome"
              :emoji "🤝"}
             {:name     "onboarding"
              :emoji    "🍑"
              :locked?  true
              :on-press #((open-token-gating-mocked
                           "onboarding"
                           "🍑"
                           (colors/custom-color :pink 50)))}
             {:name     "intro"
              :emoji    "🦄"
              :locked?  true
              :on-press #((open-token-gating-mocked
                           "intro"
                           "🦄"
                           (colors/custom-color :pink 50)))}]
   :General [{:name  "general"
              :emoji "🐷"}
             {:name     "people-ops"
              :emoji    "🌏"
              :locked?  true
              :on-press #((open-token-gating-mocked
                           "onboarding"
                           "🌏"
                           (colors/custom-color :blue 50)))}
             {:name  "announcements"
              :emoji "🎺"}]
   :Mobile  [{:name  "mobile"
              :emoji "👽"}
             {:name  "mobile-ui"
              :emoji "👽"}
             {:name  "mobile-ui-reviews"
              :emoji "👽"}]
   :Desktop [{:name  "desktop"
              :emoji "👽"}
             {:name  "desktop-ui"
              :emoji "👽"}
             {:name  "desktop-ui-reviews"
              :emoji "👽"}
             {:name  "desktop2"
              :emoji "👽"}
             {:name  "desktop-ui2"
              :emoji "👽"}
             {:name  "desktop-ui2-reviews"
              :emoji "👽"}]})

(defn channel-list-component-fn
  [channel-heights first-channel-height]
  [rn/view
   {:on-layout #(swap! first-channel-height
                  (fn []
                    (+ (if platform/ios?
                         0
                         38)
                       (int (Math/ceil (oops/oget % "nativeEvent.layout.y"))))))
    :style     {:margin-top 20 :flex 1}}
   (map-indexed (fn [index category]
                  (let [first-category (first category)]
                    ^{:key first-category}
                    [rn/view
                     {:flex      1
                      :key       (str index first-category)
                      :on-layout #(swap! channel-heights
                                    (fn []
                                      (sort-by :height
                                               (conj @channel-heights
                                                     {:height (int (oops/oget % "nativeEvent.layout.y"))
                                                      :label  first-category}))))}

                     [quo/divider-label
                      {:label            first-category
                       :chevron-position :left}]
                     [rn/view
                      {:margin-left   8
                       :margin-top    10
                       :margin-bottom 8}
                      (map-indexed (fn [inner-index channel-data]
                                     [rn/view
                                      {:key        (str inner-index (:name channel-data))
                                       :margin-top 4}
                                      [quo/channel-list-item channel-data]])
                                   (first-category mock-list-of-channels))]]))
                mock-list-of-channels)])

(def channel-list-component (memoize channel-list-component-fn))

(defn request-to-join-text
  [is-open?]
  (if is-open?
    (i18n/label :t/join-open-community)
    (i18n/label :t/request-to-join-community)))

(defn join-community
  [{:keys [joined can-join? requested-to-join-at
           community-color permissions]
    :as   community}]
  (let [pending?      (pos? requested-to-join-at)
        is-open?      (not= constants/community-channel-access-on-request (:access permissions))
        node-offline? (and can-join? (not joined) (pos? requested-to-join-at))]
    [:<>
     (when-not (or joined pending?)
       [quo/button
        {:on-press                  #(rf/dispatch
                                      [:bottom-sheet/show-sheet
                                       {:content        (fn [] [join-menu/request-to-join
                                                                community])
                                        :content-height 300}])
         :accessibility-label       :show-request-to-join-screen-button
         :override-background-color community-color
         :style                     style/join-button
         :before                    :i/communities}
        (request-to-join-text is-open?)])

     (when (and (not (or joined pending?)) (not (or is-open? node-offline?)))
       [quo/text
        {:size  :paragraph-2
         :style style/review-notice}
        (i18n/label :t/community-admins-will-review-your-request)])

     (when node-offline?
       [quo/information-box
        {:type  :informative
         :icon  :i/info
         :style {:margin-top 12}}
        (i18n/label :t/request-processed-after-node-online)])]))

(defn get-tag
  [joined]
  [quo/status-tag
   {:status {:type (if joined :positive :pending)}
    :label  (if joined
              (i18n/label :t/joined)
              (i18n/label :t/pending))}])

(defn render-page-content
  [{:keys [name description locked joined images
           status tokens tags requested-to-join-at]
    :as   community}
   channel-heights first-channel-height]
  (let [pending?        (pos? requested-to-join-at)
        thumbnail-image (get-in images [:thumbnail])]
    (fn []
      [rn/view
       [rn/view {:padding-horizontal 20}
        (when (and (not joined)
                   (not pending?)
                   (= status :gated))
          [rn/view
           {:position :absolute
            :top      8
            :right    8}
           [quo/permission-tag-container
            {:locked   locked
             :status   status
             :tokens   tokens
             :on-press #(rf/dispatch
                         [:bottom-sheet/show-sheet
                          {:content-height 210
                           :content
                           (fn []
                             [quo/token-gating
                              {:community {:name             name
                                           :community-color  colors/primary-50
                                           :community-avatar thumbnail-image
                                           :gates            {:join [{:token          "KNC"
                                                                      :token-img-src  knc-token-img
                                                                      :amount         200
                                                                      :is-sufficient? true}
                                                                     {:token          "MANA"
                                                                      :token-img-src  mana-token-img
                                                                      :amount         10
                                                                      :is-sufficient? false
                                                                      :is-purchasable true}
                                                                     {:token          "RARE"
                                                                      :token-img-src  rare-token-img
                                                                      :amount         10
                                                                      :is-sufficient?
                                                                      false}]}}}])}])}]])
        (when (or pending? joined)
          [rn/view
           {:position :absolute
            :top      12
            :right    12}
           [get-tag joined]])
        [rn/view {:margin-top 56}
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
          :weight              :regular
          :size                :paragraph-1
          :style               {:margin-top 8 :margin-bottom 12}}
         description]
        [quo/community-stats-column :card-view]
        [rn/view {:margin-top 12}]
        [quo/community-tags tags]
        [preview-user-list]
        [join-community community]]
       [channel-list-component channel-heights first-channel-height]])))

(defn render-sticky-header
  [channel-heights first-channel-height]
  (fn [scroll-height]
    (when (> scroll-height @first-channel-height)
      [blur/view
       {:blur-amount   32
        :blur-type     :xlight
        :overlay-color (if platform/ios? colors/white-opa-70 :transparent)
        :style         style/blur-channel-header}
       [quo/divider-label
        {:label            (:label (last (filter (fn [{:keys [height]}]
                                                   (>= scroll-height (+ height @first-channel-height)))
                                                 @channel-heights)))
         :chevron-position :left}]])))

(defn community-card-page-view
  [{:keys [name images id] :as community}]
  (let [channel-heights      (reagent/atom [])
        first-channel-height (reagent/atom 0)
        scroll-component     (scroll-page/scroll-page
                              {:uri (get-in images [:large :uri])}
                              {:right-section-buttons [{:icon             :i/options
                                                        :background-color (scroll-page/icon-color)
                                                        :on-press
                                                        #(rf/dispatch
                                                          [:bottom-sheet/show-sheet
                                                           {:content
                                                            (fn []
                                                              [options/community-options-bottom-sheet
                                                               id])}])}]}
                              name)]
    (fn []
      (let [page-component (memoize (render-page-content community channel-heights first-channel-height))
            sticky-header  (memoize (render-sticky-header channel-heights first-channel-height))]
        (fn []
          (scroll-component
           sticky-header
           page-component))))))

(defn overview
  []
  (let [id        (rf/sub [:get-screen-params :community-overview])
        community (rf/sub [:communities/community id])]
    [rn/view
     {:style
      {:position :absolute
       :top      (if platform/ios? 0 44)
       :width    "100%"
       :height   "110%"}}
     [community-card-page-view community]]))

