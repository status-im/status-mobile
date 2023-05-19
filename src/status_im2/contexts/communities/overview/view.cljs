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
            [status-im2.contexts.communities.actions.community-options.view :as options]
            [quo2.components.navigation.floating-shell-button :as floating-shell-button]
            [status-im2.contexts.communities.overview.utils :as utils]
            [status-im2.common.password-authentication.view :as password-authentication]
            [utils.re-frame :as rf]))

(defn preview-user-list
  [user-list]
  (when (seq user-list)
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
      (utils/join-existing-users-string user-list)]]))

(defn add-category-height
  [categories-heights category height]
  (swap! categories-heights
    (fn [heights]
      (assoc heights category height))))

(defn collapse-category
  [community-id category-id collapsed?]
  (rf/dispatch [:communities/toggle-collapsed-category community-id category-id (not collapsed?)]))

(defn layout-y
  [event]
  (oops/oget event "nativeEvent.layout.y"))

(defn channel-list-component
  [{:keys [on-category-layout community-id on-first-channel-height-changed]}
   channels-list]
  [rn/view
   {:on-layout #(on-first-channel-height-changed
                 (+ (if platform/ios? 0 38)
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

(defn join-gated-community
  [id]
  (rf/dispatch [:password-authentication/show
                {:content (fn [] [password-authentication/view])}
                {:label    (i18n/label :t/join-open-community)
                 :on-press #(rf/dispatch [:communities/request-to-join-with-password id %])}]))

(defn info-button
  []
  [rn/touchable-without-feedback
   {:on-press
    #(rf/dispatch
      [:show-bottom-sheet
       {:content
        (fn []
          [quo/documentation-drawers
           {:title        (i18n/label :t/token-gated-communities)
            :show-button? true
            :button-label (i18n/label :t/read-more)
            :button-icon  :info}
           [quo/text (i18n/label :t/token-gated-communities-info)]])}])}
   [rn/view
    [quo/icon :i/info {:no-color true}]]])

(defn token-gates
  [{:keys [id]}]
  (rf/dispatch [:communities/check-permissions-to-join-community id])
  (fn [{:keys [id community-color]}]
    (let [{:keys [can-request-access?
                  number-of-hold-tokens tokens]} (rf/sub [:community/token-gated-overview id])]
      [rn/view {:style (style/token-gated-container)}
       [rn/view
        {:style {:padding-horizontal 12
                 :flex-direction     :row
                 :align-items        :center
                 :justify-content    :space-between
                 :flex               1}}
        [quo/text {:weight :medium}
         (if can-request-access?
           (i18n/label :t/you-eligible-to-join)
           (i18n/label :t/you-not-eligible-to-join))]
        [info-button]]
       [quo/text {:style {:padding-horizontal 12 :padding-bottom 18} :size :paragraph-2}
        (if can-request-access?
          (i18n/label :t/you-hold-number-of-hold-tokens-of-these
                      {:number-of-hold-tokens number-of-hold-tokens})
          (i18n/label :t/you-must-hold))]
       [quo/token-requirement-list
        {:tokens   tokens
         :padding? true}]
       [quo/button
        {:on-press                  #(join-gated-community id)
         :accessibility-label       :join-community-button
         :override-background-color community-color
         :style                     {:margin-horizontal 12 :margin-top 12 :margin-bottom 12}
         :disabled                  (not can-request-access?)
         :before                    (if can-request-access? :i/unlocked :i/locked)}
        (i18n/label :t/join-open-community)]])))

(defn join-community
  [{:keys [joined can-join? community-color permissions token-permissions] :as community}
   pending?]
  (let [access-type     (get-access-type (:access permissions))
        unknown-access? (= access-type :unknown-access)
        invite-only?    (= access-type :invite-only)
        is-open?        (= access-type :open)
        node-offline?   (and can-join? (not joined) pending?)]
    [:<>
     (when-not (or joined pending? invite-only? unknown-access?)
       (if token-permissions
         [token-gates community]
         [quo/button
          {:on-press                  #(rf/dispatch [:open-modal :community-requests-to-join community])
           :accessibility-label       :show-request-to-join-screen-button
           :override-background-color community-color
           :before                    :i/communities}
          (request-to-join-text is-open?)]))

     (when (and (not (or joined pending? token-permissions)) (not (or is-open? node-offline?)))
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

(defn status-tag
  [pending? joined]
  (when (or pending? joined)
    [rn/view {:position :absolute :top 12 :right 12}
     [quo/status-tag
      {:status {:type (if joined :positive :pending)}
       :label  (if joined
                 (i18n/label :t/joined)
                 (i18n/label :t/pending))}]]))

(defn add-on-press-handler
  [community-id {:keys [id locked?] :or {locked? false} :as chat}]
  (merge
   chat
   (when (and (not locked?) id)
     {:on-press (fn []
                  (rf/dispatch [:dismiss-keyboard])
                  (rf/dispatch [:chat/navigate-to-chat (str community-id id)]))})))

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
    :size                :heading-1
    :style               {:margin-top 56}}
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
  [{:keys [name description joined tags id]
    :as   community}
   pending?
   {:keys [on-category-layout on-first-channel-height-changed]}]
  (let [chats-by-category (rf/sub [:communities/categorized-channels id])]
    [:<>
     [rn/view {:padding-horizontal 20}
      [status-tag pending? joined]
      [community-header name]
      [community-description description]
      ;; [quo/community-stats-column :card-view] not implemented
      [rn/view {:margin-top 12}]
      [quo/community-tags tags]
      ;;[preview-user-list users] not implemented
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
      [rn/view
       [blur/webview-blur
        {:style style/blur-channel-header
         :blur-radius (if platform/ios? 20 10)}]
      ;[blur/view
      ; {:style         style/blur-channel-header
      ;  :blur-amount   20
      ;  :blur-type     :transparent
      ;  :overlay-color :transparent}
       [quo/divider-label
        {:label            label
         :chevron-position :left}]])))

(defn page-nav-right-section-buttons
  [id]
  [{:icon                :i/options
    :background-color    (scroll-page/icon-color)
    :accessibility-label :community-options-for-community
    :on-press            #(rf/dispatch
                           [:show-bottom-sheet
                            {:content (fn []
                                        [options/community-options-bottom-sheet id])}])}])

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
