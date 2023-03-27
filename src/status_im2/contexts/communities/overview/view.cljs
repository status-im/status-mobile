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
            [quo2.components.navigation.floating-shell-button :as floating-shell-button]
            [status-im2.contexts.communities.overview.utils :as utils]
            [utils.re-frame :as rf]))

(defn preview-user-list
  [user-list]
  [rn/view style/preview-user
   [quo/preview-list
    {:type      :user
     :list-size (count user-list)
     :size      24}
    user-list]
   [quo/text
    {:accessibility-label :communities-screen-title
     :style               {:margin-left 8}
     :size                :label}
    (utils/join-existing-users-string user-list)]])

(defn channel-token-gating-details
  [name token-gating emoji channel-color]
  [rn/view {:height 350 :margin-top 20}
   [quo/token-gating
    {:channel {:name                   name
               :community-color        channel-color
               :emoji                  emoji
               :emoji-background-color channel-color
               :on-enter-channel       (fn []
                                         (js/alert
                                          "Entered channel"
                                          "Wuhuu!! You successfully entered the channel :)"))
               :gates                  token-gating}}]])

(defn open-channel-token-gating-details
  [name token-gating emoji channel-color]
  (rf/dispatch
   [:bottom-sheet/show-sheet
    {:content
     (fn []
       [channel-token-gating-details name token-gating emoji channel-color])
     :content-height 210}]))

(defn layout-y
  [event]
  (oops/oget event "nativeEvent.layout.y"))

(defn add-category-height
  [categories-heights category height]
  (swap! categories-heights
    (fn [heights]
      (assoc heights category height))))

(defn collapse-category
  [community-id category-id collapsed?]
  (rf/dispatch [:communities/toggle-collapsed-category community-id category-id (not collapsed?)]))

(defn channel-list-component
  [{:keys [on-category-layout
           community-id
           on-first-channel-height-changed]}
   channels-list]
  [rn/view
   {:on-layout #(on-first-channel-height-changed (+ (if platform/ios?
                                                      0
                                                      38)
                                                    (int (Math/ceil (layout-y %))))
                                                 (into #{} (map (comp :name second) channels-list)))
    :style     {:margin-top 20 :flex 1}}
   (map
    (fn [[category-id {:keys [chats name collapsed?]}]]
      [rn/view
       {:flex      1
        :key       category-id
        ;; on-layout fires only when the component re-renders, so
        ;; in case the category hasn't changed, it will not be fired
        :on-layout #(on-category-layout name (int (layout-y %)))}

       (when-not (= constants/empty-category-id category-id)
         [quo/divider-label
          {:label            name
           :on-press         #(collapse-category
                               community-id
                               category-id
                               collapsed?)
           :chevron-icon     (if collapsed? :main-icons/chevron-right :main-icons/chevron-down)
           :padding-bottom   (if collapsed? 7 0)
           :chevron-position :left}])
       (when-not collapsed?
         [rn/view
          {:margin-left   8
           :margin-top    10
           :margin-bottom 8}
          (map (fn [channel]
                 [rn/view
                  {:key        (:id channel)
                   :margin-top 4}
                  [quo/channel-list-item channel]])
               chats)])])
    channels-list)])

(defn request-to-join-text
  [is-open?]
  (if is-open?
    (i18n/label :t/join-open-community)
    (i18n/label :t/request-to-join-community)))

(defn get-access-type
  [access]
  (case access
    constants/community-no-membership-access   :open
    constants/community-invitation-only-access :invite-only
    constants/community-on-request-access      :request-access
    :unknown-access))

(defn join-community
  [{:keys [joined can-join?
           community-color permissions]
    :as   community} pending?]
  (let [access-type     (get-access-type (:access permissions))
        unknown-access? (= access-type :unknown-access)
        invite-only?    (= access-type :invite-only)
        is-open?        (= access-type :open)
        node-offline?   (and can-join? (not joined) pending?)]
    [:<>
     (when-not (or joined pending? invite-only? unknown-access?)
       [quo/button
        {:on-press                  #(rf/dispatch
                                      [:bottom-sheet/show-sheet
                                       {:content                   (fn [] [join-menu/request-to-join
                                                                           community])
                                        :bottom-safe-area-spacing? false
                                        :content-height            300}])
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

(defn community-token-gating-details
  [name thumbnail-image tokens]
  [rn/view {:height 200 :margin-top 20}
   [quo/token-gating
    {:community {:name                     name
                 :community-color          colors/primary-50
                 :community-avatar-img-src thumbnail-image
                 :gates                    tokens}}]])

(defn add-on-press-handler
  [community-id {:keys [name emoji id locked? token-gating] :or {locked? false} :as chat}]
  (merge
   chat
   (if (and locked? token-gating)
     {:on-press #(open-channel-token-gating-details
                  name
                  token-gating
                  emoji
                  (colors/custom-color :pink 50))}

     (when (and (not locked?) id)
       {:on-press (fn []
                    (rf/dispatch [:dismiss-keyboard])
                    (rf/dispatch [:chat/navigate-to-chat (str community-id id)])
                    (rf/dispatch [:search/home-filter-changed nil]))}))))

(defn add-on-press-handler-to-chats
  [community-id chats]
  (mapv (partial add-on-press-handler community-id) chats))

(defn add-on-press-handler-to-categorized-chats
  [community-id categorized-chats]
  (let [add-on-press (partial add-on-press-handler-to-chats community-id)]
    (map (fn [[category v]]
           [category (update v :chats add-on-press)])
         categorized-chats)))

(defn community-header
  [name]
  [quo/text
   {:accessibility-label :chat-name-text
    :number-of-lines     1
    :ellipsize-mode      :tail
    :weight              :semi-bold
    :size                :heading-1}
   name])

(defn community-description
  [description]
  [quo/text
   {:accessibility-label :community-description-text
    :number-of-lines     2
    :ellipsize-mode      :tail
    :weight              :regular
    :size                :paragraph-1
    :style               {:margin-top 8 :margin-bottom 12}}
   description])

(defn community-content
  [{:keys [name description locked joined images
           status tokens tags id]
    :as   community}
   pending?
   {:keys [on-category-layout
           on-first-channel-height-changed]}]
  (let [thumbnail-image   (:thumbnail images)
        chats-by-category (rf/sub [:communities/categorized-channels id])
        users             (rf/sub [:communities/users id])]
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
                           [community-token-gating-details
                            name
                            thumbnail-image
                            tokens])}])}]])
      (when (or pending? joined)
        [rn/view
         {:position :absolute
          :top      12
          :right    12}
         [get-tag joined]])
      [rn/view {:margin-top 56}
       [community-header name]]
      [community-description description]
      [quo/community-stats-column :card-view]
      [rn/view {:margin-top 12}]
      [quo/community-tags tags]
      [preview-user-list users]
      [join-community community pending?]]
     [channel-list-component
      {:on-category-layout              on-category-layout
       :community-id                    id
       :on-first-channel-height-changed on-first-channel-height-changed}
      (add-on-press-handler-to-categorized-chats id chats-by-category)]]))

(defn sticky-category-header
  [_]
  (fn [{:keys [enabled label]}]
    (when enabled
      [blur/view
       {:style         style/blur-channel-header
        :blur-amount   20
        :blur-type     :transparent
        :overlay-color :transparent}
       [quo/divider-label
        {:label            label
         :chevron-position :left}]])))

(defn page-nav-right-section-buttons
  [id]
  [{:icon                :i/options
    :background-color    (scroll-page/icon-color)
    :accessibility-label :community-options-for-community
    :on-press            #(rf/dispatch
                           [:bottom-sheet/show-sheet
                            {:content (fn []
                                        [options/community-options-bottom-sheet
                                         id])}])}])

(defn pick-first-category-by-height
  [scroll-height first-channel-height categories-heights]
  (->> categories-heights
       (sort-by (comp - second))
       (some (fn [[category height]]
               (and
                (>= scroll-height (+ height first-channel-height))
                category)))))

(defn community-card-page-view
  [{:keys [name images id]}]
  (let [categories-heights   (reagent/atom {})
        first-channel-height (reagent/atom 0)
        scroll-height        (reagent/atom 0)
        cover                {:uri (get-in images [:banner :uri])}
        logo                 {:uri (get-in images [:thumbnail :uri])}]
    (fn [community pending?]
      [scroll-page/scroll-page
       {:cover-image                    cover
        :logo                           logo
        :page-nav-right-section-buttons (page-nav-right-section-buttons id)
        :name                           name
        :on-scroll                      #(reset! scroll-height %)
        :navigate-back?                 true
        :background-color               (colors/theme-colors
                                         colors/white
                                         colors/neutral-90)
        :height                         (if platform/ios?
                                          100
                                          148)}

       [sticky-category-header
        {:enabled (> @scroll-height @first-channel-height)
         :label   (pick-first-category-by-height
                   @scroll-height
                   @first-channel-height
                   @categories-heights)}]

       [community-content
        community
        pending?
        {:on-category-layout              (partial add-category-height categories-heights)
         :on-first-channel-height-changed
         ;; Here we set the height of the component
         ;; and we filter out the categories, as some might have been removed
         (fn [height categories]
           (swap! categories-heights select-keys categories)
           (reset! first-channel-height height))}]])))

(defn overview
  []
  (let [id        (rf/sub [:get-screen-params :community-overview])
        community (rf/sub [:communities/community id])
        pending?  (rf/sub [:communities/my-pending-request-to-join id])]
    [rn/view
     {:style style/community-overview-container}
     [community-card-page-view community pending?]
     [floating-shell-button/floating-shell-button
      {:jump-to {:on-press #(rf/dispatch [:shell/navigate-to-jump-to])
                 :label    (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   41}]]))
