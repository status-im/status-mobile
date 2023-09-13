(ns status-im2.contexts.communities.overview.view
  (:require [oops.core :as oops]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.home.actions.view :as actions]
            [status-im2.common.password-authentication.view :as password-authentication]
            [status-im2.common.scroll-page.style :as scroll-page.style]
            [status-im2.common.scroll-page.view :as scroll-page]
            [status-im2.constants :as constants]
            [status-im2.contexts.communities.actions.chat.view :as chat-actions]
            [status-im2.contexts.communities.actions.community-options.view :as options]
            [status-im2.contexts.communities.overview.style :as style]
            [status-im2.contexts.communities.overview.utils :as utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn preview-user-list
  [user-list]
  (when (seq user-list)
    [rn/view style/preview-user
     [quo/preview-list
      {:type   :user
       :number (count user-list)
       :size   :size/s-24}
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

(defn- channel-chat-item
  [community-id community-color
   {:keys [name emoji muted? id mentions-count unread-messages? on-press locked?] :as chat}]
  (let [sheet-content      [actions/chat-actions
                            (assoc chat
                                   :chat-type constants/community-chat-type
                                   :chat-id   (str community-id id))
                            false]
        notification       (cond
                             muted?               :mute
                             (> mentions-count 0) :mention
                             unread-messages?     :notification
                             :else                nil)
        channel-options    {:name                name
                            :emoji               emoji
                            :customization-color community-color
                            :mentions-count      mentions-count
                            :locked?             locked?
                            :notification        notification}
        channel-sheet-data {:selected-item (fn [] [quo/channel channel-options])
                            :content       (fn [] sheet-content)}]
    [rn/view {:key id}
     [quo/channel
      (merge channel-options
             {:on-press      on-press
              :on-long-press #(rf/dispatch [:show-bottom-sheet channel-sheet-data])})]]))

(defn channel-list-component
  [{:keys [on-category-layout community-id community-color on-first-channel-height-changed]}
   channels-list]
  [rn/view
   {:on-layout #(on-first-channel-height-changed
                 (+ 38 (int (Math/ceil (layout-y %))))
                 (into #{} (map (comp :name second) channels-list)))
    :style     {:margin-top 8 :flex 1}}
   (doall
    (for [[category-id {:keys [chats name collapsed?]}] channels-list]
      [rn/view
       {:key       category-id
        ;; on-layout fires only when the component re-renders, so
        ;; in case the category hasn't changed, it will not be fired
        :on-layout #(on-category-layout name (int (layout-y %)))}
       (when-not (= constants/empty-category-id category-id)
         [quo/divider-label
          {:on-press     #(collapse-category community-id category-id collapsed?)
           :chevron-icon (if collapsed? :i/chevron-right :i/chevron-down)
           :chevron      :left}
          name])
       (when-not collapsed?
         (into [rn/view {:style {:padding-horizontal 8 :padding-bottom 8}}]
               (map #(channel-chat-item community-id community-color %))
               chats))]))])

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
  (fn [{:keys [id color]}]
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
        {:on-press            #(join-gated-community id)
         :accessibility-label :join-community-button
         :customization-color color
         :container-style     {:margin-horizontal 12 :margin-top 12 :margin-bottom 12}
         :disabled?           (not can-request-access?)
         :icon-left           (if can-request-access? :i/unlocked :i/locked)}
        (i18n/label :t/join-open-community)]])))

(defn join-community
  [{:keys [joined color permissions token-permissions] :as community}
   pending?]
  (let [access-type     (get-access-type (:access permissions))
        unknown-access? (= access-type :unknown-access)
        invite-only?    (= access-type :invite-only)]
    [:<>
     (when-not (or joined pending? invite-only? unknown-access?)
       (if (seq token-permissions)
         [token-gates community]
         [quo/button
          {:on-press            #(rf/dispatch [:open-modal :community-requests-to-join community])
           :accessibility-label :show-request-to-join-screen-button
           :customization-color color
           :icon-left           :i/communities}
          (i18n/label :t/request-to-join-community)]))

     (when (not (or joined pending? token-permissions))
       [quo/text
        {:size   :paragraph-2
         :weight :regular
         :style  style/review-notice}
        (i18n/label :t/community-admins-will-review-your-request)])]))

(defn status-tag
  [pending? joined]
  (when (or pending? joined)
    [rn/view {:style {:position :absolute :top 12 :right 12}}
     [quo/status-tag
      {:status {:type (if joined :positive :pending)}
       :label  (if joined
                 (i18n/label :t/joined)
                 (i18n/label :t/pending))}]]))

(defn add-handlers
  [community-id
   {:keys [id locked?]
    :or   {locked? false}
    :as   chat}]
  (merge
   chat
   (when (and (not locked?) id)
     {:on-press      (fn []
                       (rf/dispatch [:dismiss-keyboard])
                       (rf/dispatch [:chat/navigate-to-chat (str community-id id)]))
      :on-long-press #(rf/dispatch
                       [:show-bottom-sheet
                        {:content (fn []
                                    [chat-actions/actions chat false])}])
      :community-id  community-id})))

(defn add-handlers-to-chats
  [community-id chats]
  (mapv (partial add-handlers community-id) chats))

(defn add-handlers-to-categorized-chats
  [community-id categorized-chats]
  (let [add-on-press (partial add-handlers-to-chats community-id)]
    (map (fn [[category v]]
           [category (update v :chats add-on-press)])
         categorized-chats)))

(defn community-header
  [title logo description]
  [quo/text-combinations
   {:container-style
    {:margin-top
     (if logo
       12
       (+ scroll-page.style/picture-radius
          scroll-page.style/picture-border-width
          12))
     :margin-bottom 12}
    :avatar logo
    :title title
    :description description
    :title-accessibility-label :community-title
    :description-accessibility-label :community-description}])

(defn community-content
  [{:keys [name description joined images tags color id]
    :as   community}
   pending?
   {:keys [on-category-layout
           collapsed?
           on-first-channel-height-changed]}]
  (let [chats-by-category (rf/sub [:communities/categorized-channels id])]
    [:<>
     [rn/view {:style style/community-content-container}
      (when-not collapsed?
        [status-tag pending? joined])
      [community-header name (when collapsed? (get-in images [:thumbnail :uri]))
       (when-not collapsed? description)]
      (when-not collapsed?
        [quo/community-tags
         {:tags            tags
          :last-item-style style/last-community-tag
          :container-style style/community-tag-container}])
      [join-community community pending?]]
     [channel-list-component
      {:on-category-layout              on-category-layout
       :community-id                    id
       :community-color                 color
       :on-first-channel-height-changed on-first-channel-height-changed}
      (add-handlers-to-categorized-chats id chats-by-category)]]))

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
        {:chevron :left}
        label]])))

(defn page-nav-right-section-buttons
  [id]
  [{:icon-name           :i/options
    :accessibility-label :community-options-for-community
    :on-press            #(rf/dispatch
                           [:show-bottom-sheet
                            {:content (fn [] [options/community-options-bottom-sheet id])}])}])

(defn pick-first-category-by-height
  [scroll-height first-channel-height categories-heights]
  (->> categories-heights
       (sort-by (comp - second))
       (some (fn [[category height]]
               (and (>= scroll-height (+ height first-channel-height))
                    category)))))

(defn community-scroll-page
  [{:keys [joined]}]
  (let [scroll-height        (reagent/atom 0)
        categories-heights   (reagent/atom {})
        first-channel-height (reagent/atom 0)
        ;; We track the initial value of joined
        ;; as we open the page to avoid switching
        ;; from not collapsed to collapsed if the
        ;; user is on this page
        initial-joined?      joined]
    (fn [{:keys [id name images] :as community} pending?]
      (let [cover      {:uri (get-in images [:banner :uri])}
            logo       {:uri (get-in images [:thumbnail :uri])}
            collapsed? (and initial-joined? (:joined community))]
        [scroll-page/scroll-page
         {:cover-image                    cover
          :collapsed?                     collapsed?
          :logo                           logo
          :page-nav-right-section-buttons (page-nav-right-section-buttons id)
          :name                           name
          :on-scroll                      #(reset! scroll-height %)
          :navigate-back?                 true
          :background-color               (colors/theme-colors colors/white colors/neutral-95)
          :height                         148}
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
           :collapsed?                      collapsed?
           :on-first-channel-height-changed
           ;; Here we set the height of the component and we filter out the
           ;; categories, as some might have been removed
           (fn [height categories]
             (swap! categories-heights select-keys categories)
             (reset! first-channel-height height))}]]))))

(defn community-card-page-view
  [id]
  (let [{:keys [id]
         :as   community} (rf/sub [:communities/community id])
        pending?          (rf/sub [:communities/my-pending-request-to-join id])]
    [community-scroll-page community pending?]))

(defn overview
  [id]
  (let [id                  (or id (rf/sub [:get-screen-params :community-overview]))
        customization-color (rf/sub [:profile/customization-color])]
    [rn/view {:style style/community-overview-container}
     [community-card-page-view id]
     [quo/floating-shell-button
      {:jump-to {:on-press            #(rf/dispatch [:shell/navigate-to-jump-to])
                 :customization-color customization-color
                 :label               (i18n/label :t/jump-to)}}
      {:position :absolute
       :bottom   34}]]))
