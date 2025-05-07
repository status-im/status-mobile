(ns status-im.contexts.communities.overview.view
  (:require
    [oops.core :as oops]
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events.helper]
    [status-im.common.home.actions.view :as actions]
    [status-im.common.resources :as resources]
    [status-im.constants :as constants]
    [status-im.contexts.communities.actions.community-options.view :as options]
    [status-im.contexts.communities.overview.style :as style]
    [status-im.contexts.communities.utils :as communities.utils]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.worklets.communities :as worklets]))

;; NOTE: values compared against `scroll-amount` to trigger animations.
(def expand-header-threshold
  "Dragging distance to collapse/extend the community."
  150)

(def sheet-displacement-threshold
  "Dragging distance to round sheet borders and move the sheet 8 units."
  (+ expand-header-threshold 20))

(def text-movement-threshold
  "Dragging distance to start the text movement from/to the bottom to/from the right."
  (* expand-header-threshold 0.7))

(def info-opacity-threshold
  "Dragging distance to appear/disappear the community info (description, tags & stats)."
  (* expand-header-threshold 0.5))

(def snap-header-threshold
  "Threshold to automatically move the header to a collapsed/expanded state and avoid an
  intermediate state."
  (* expand-header-threshold 0.75))

(def expand-header-limit
  "Max dragging distance where the header animation ends. It works to identify when to
  start the flat-list scrolling."
  (+ sheet-displacement-threshold 56))

(defn- collapse-category
  [community-id category-id collapsed?]
  (rf/dispatch
   [:communities/toggle-collapsed-category community-id category-id (not collapsed?)]))

(defn- get-access-type
  [access]
  (condp = access
    constants/community-no-membership-access   :open
    constants/community-invitation-only-access :invite-only
    constants/community-on-request-access      :request-access
    :unknown-access))

(defn- show-join-modal
  [community-id]
  (fn []
    (rf/dispatch [:open-modal :screen/community-account-selection-sheet
                  {:community-id community-id}])))

(defn token-gated-communities-info
  []
  [quo/documentation-drawers {:title (i18n/label :t/token-gated-communities)}
   [quo/text {:size :paragraph-2}
    (i18n/label :t/token-gated-communities-info)]])

(defn- on-join-info-press
  []
  (rf/dispatch [:show-bottom-sheet {:content token-gated-communities-info}]))

(defn- token-requirements
  [community-id role-permissions? color tags?]
  (let [{:keys [can-request-access? no-member-permission? networks-not-supported?
                highest-permission-role
                tokens]}        (rf/sub [:community/token-gated-overview community-id])
        on-request-access-press (show-join-modal community-id)]
    (cond
      networks-not-supported?
      [rn/view {:style (style/request-to-join-button tags?)}
       [quo/text (i18n/label :t/network-not-supported)]]

      (or (not role-permissions?) no-member-permission?)
      [rn/view {:style (style/request-to-join-button tags?)}
       [quo/button
        {:on-press            on-request-access-press
         :accessibility-label :show-request-to-join-screen-button
         :customization-color color
         :icon-left           :i/communities}
        (i18n/label :t/request-to-join)]]

      :else
      (let [highest-role-text (i18n/label
                               (communities.utils/role->translation-key
                                highest-permission-role
                                :t/member))]
        [rn/view {:style style/request-to-join-as}
         [quo/community-token-gating
          {:role            highest-role-text
           :tokens          tokens
           :community-color color
           :satisfied?      can-request-access?
           :on-press        on-request-access-press
           :on-press-info   on-join-info-press}]]))))

(defn- join-community
  [{:keys [community-id joined? permissions role-permissions? color tags?]}]
  (let [pending?    (rf/sub [:communities/my-pending-request-to-join community-id])
        access-type (get-access-type (:access permissions))]
    (when-not (or joined? pending? (#{:unknown-access :invite-only} access-type))
      [token-requirements community-id role-permissions? color tags?])))

(defn- status-tag
  [{:keys [community-id joined? info-styles]}]
  (let [pending? (rf/sub [:communities/my-pending-request-to-join community-id])]
    (when (or pending? joined?)
      [reanimated/view {:style [style/status-tag-position info-styles]}
       [quo/status-tag
        {:status {:type (if joined? :positive :pending)}
         :label  (if joined? (i18n/label :t/joined) (i18n/label :t/pending))}]])))

(defn- category-divider
  [community-id
   {collapsed?    :collapsed?
    category-name :name
    category-id   :id}]
  (let [theme (quo.context/use-theme)]
    [rn/view
     {:style         (style/category-divider theme)
      :blur-amount   20
      :blur-type     :transparent
      :overlay-color :transparent}
     [quo/divider-label
      {:on-press     (fn [] (collapse-category community-id category-id collapsed?))
       :chevron-icon (if collapsed? :i/chevron-right :i/chevron-down)
       :chevron      :left
       :blur?        true}
      category-name]]))

(defn- notification-type
  [{:keys [muted? mentions-count unread-messages?] :as _chat}]
  (cond
    muted?               :mute
    (> mentions-count 0) :mention
    unread-messages?     :notification))

(defn- navigate-to-chat
  [chat-id]
  (rf/dispatch [:dismiss-keyboard])
  (debounce/throttle-and-dispatch
   [:communities/navigate-to-community-chat chat-id]
   1000))

(defn- show-chat-actions
  [chat-data]
  (rf/dispatch
   [:show-bottom-sheet
    {:selected-item (fn [] [quo/channel chat-data])
     :content       (fn [] [actions/chat-actions chat-data false])}]))

(defn channel-item
  [{:keys      [color locked?]
    channel-id :id
    :as        chat}
   {:keys [community-id joined-or-spectated?] :as _community-data}]
  [rn/view {:style {:padding-horizontal 8}}
   (let [chat-id       (str community-id channel-id)
         chat-data     (assoc chat
                              :community-id        community-id
                              :chat-type           constants/community-chat-type
                              :chat-id             chat-id
                              :customization-color color
                              :notification        (notification-type chat))
         on-press      (rn/use-callback
                        (fn []
                          (when (and (not locked?) channel-id joined-or-spectated?)
                            (navigate-to-chat chat-id)))
                        [joined-or-spectated? locked? channel-id])
         on-long-press (rn/use-callback
                        (fn []
                          (when (and (not locked?) channel-id)
                            (show-chat-actions chat-data)))
                        [locked? channel-id])]
     [quo/channel
      (assoc chat-data
             :on-press      on-press
             :on-long-press on-long-press)])])

(defn channel-listing-item
  [{:keys [community-id] :as community-data}]
  (fn [{:keys [render-as] :as item-data} _ _ _]
    (case render-as
      :separator [rn/view {:style {:height 8}}]
      :category  [category-divider community-id item-data]
      :channel   [channel-item item-data community-data]
      nil)))

(def channel-component-heights
  {:category  34
   :channel   48
   :separator 8})

(defn- unusable-area-height
  []
  (+ (safe-area/get-top)
     32
     11 ;;top page buttons & button's padding
     (safe-area/get-bottom)))

(defn- calc-scrollable-content
  [scrollable-height]
  (- (+ scrollable-height (unusable-area-height))
     (:height (rn/get-window))))

(defn- calc-listing-height
  []
  (- (:height (rn/get-window)) (unusable-area-height)))

(defn- channel-listing
  [{:keys [community-id scroll-amount header-height set-max-scroll]}]
  (let [theme                  (quo.context/use-theme)
        channels-styles        (worklets/use-channels-styles
                                {:scroll-amount                scroll-amount
                                 :header-height                header-height
                                 :expand-header-threshold      expand-header-threshold
                                 :sheet-displacement-threshold sheet-displacement-threshold
                                 :expand-header-limit          expand-header-limit})
        flat-list-ref          (reanimated/use-animated-ref)
        _scroll-to-animation   (worklets/use-scroll-to
                                {:animated-ref        flat-list-ref
                                 :scroll-amount       scroll-amount
                                 :expand-header-limit expand-header-limit})
        {:keys [joined?
                spectated?]}   (rf/sub [:communities/community-overview community-id])
        joined-or-spectated?   (or joined? spectated?)
        render-fn              (rn/use-callback
                                (channel-listing-item {:community-id         community-id
                                                       :joined-or-spectated? joined-or-spectated?})
                                [joined-or-spectated?])
        flatten-channels       (rf/sub [:communities/flatten-channels-and-categories community-id])
        categories-indexes     (keep-indexed (fn [idx {:keys [render-as]}]
                                               (when (= render-as :category) idx))
                                             flatten-channels)
        scrollable-area-height (->> flatten-channels
                                    (map (comp channel-component-heights :render-as))
                                    (reduce +))
        listing-height         (calc-listing-height)]
    (rn/use-effect
     (fn []
       (let [max-scroll-offset (calc-scrollable-content scrollable-area-height)]
         (if (neg? max-scroll-offset)
           (set-max-scroll 0)
           (set-max-scroll max-scroll-offset))))
     [scrollable-area-height])
    [reanimated/flat-list
     {:ref                     flat-list-ref
      :style                   [(style/channel-listing theme listing-height) channels-styles]
      :data                    flatten-channels
      :content-container-style (when platform/ios? {:padding-bottom (safe-area/get-bottom)})
      :sticky-header-indices   categories-indexes
      :scroll-enabled          false
      :render-fn               render-fn
      :key-fn                  :id}]))

(defn- header-cover-image
  [{:keys [cover-image background-color header-opacity]}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/header-cover-image background-color)}
     [reanimated/image {:style style/cover-image :source {:uri cover-image}}]
     [reanimated/view {:style (style/cover-image-blur-container header-opacity)}
      [rn/image
       {:style       style/cover-image
        :source      {:uri cover-image}
        :blur-radius 20}]
      [rn/view {:style (style/cover-image-blur-layer theme)}]]]))

(defn- open-community-options
  [community-id]
  (fn []
    (rf/dispatch
     [:show-bottom-sheet
      {:content (fn [] [options/community-options-bottom-sheet community-id])}])))

(defn- page-nav
  [{:keys [blur-version? nav-content-opacity community-name community-logo community-id]}]
  (let [on-options-press (rn/use-callback (open-community-options community-id))
        base-props       {:right-side          [{:icon-name           :i/options
                                                 :accessibility-label :community-options-for-community
                                                 :on-press            on-options-press}]
                          :icon-name           :i/close
                          :on-press            events.helper/navigate-back
                          :accessibility-label :back-button}]
    (if blur-version?
      [quo/page-nav
       (assoc base-props
              :type           :community
              :background     :blur
              :center-opacity nav-content-opacity
              :community-name community-name
              :community-logo community-logo)]
      [quo/page-nav
       (assoc base-props
              :type       :no-title
              :background :photo)])))

(defn- header
  [community-id scroll-amount]
  (let [header-opacity          (worklets/use-header-opacity
                                 {:scroll-amount                scroll-amount
                                  :expand-header-threshold      expand-header-threshold
                                  :sheet-displacement-threshold sheet-displacement-threshold})
        opposite-header-opacity (worklets/use-opposite-header-opacity header-opacity)
        nav-content-opacity     (worklets/use-nav-content-opacity
                                 {:scroll-amount                scroll-amount
                                  :sheet-displacement-threshold sheet-displacement-threshold
                                  :expand-header-limit          expand-header-limit})
        {:keys [community-name color logo
                cover-image]}   (rf/sub [:communities/community-overview community-id])]
    [:<>
     [header-cover-image
      {:cover-image      cover-image
       :background-color color
       :header-opacity   header-opacity}]
     [reanimated/view {:style (style/page-nav-container opposite-header-opacity)}
      [page-nav
       {:blur-version? false
        :community-id  community-id}]]
     [reanimated/view {:style (style/page-nav-container-blur header-opacity)}
      [page-nav
       {:blur-version?       true
        :community-id        community-id
        :nav-content-opacity nav-content-opacity
        :community-name      community-name
        :community-logo      logo}]]]))

(defn- community-logo
  [{:keys [scroll-amount community-id]}]
  (let [theme          (quo.context/use-theme)
        logo-styles    (worklets/use-logo-styles
                        {:scroll-amount                scroll-amount
                         :expand-header-threshold      expand-header-threshold
                         :sheet-displacement-threshold sheet-displacement-threshold
                         :text-movement-threshold      text-movement-threshold})
        {:keys [logo]} (rf/sub [:communities/community-overview community-id])]
    [reanimated/view
     {:style [style/community-logo
              (style/community-logo-bg-color theme)
              logo-styles]}
     [rn/image {:style style/community-logo-image :source logo}]]))

(defn- name-and-description
  [{:keys [scroll-amount community-name community-description info-styles]}]
  (let [name-styles (worklets/use-name-styles
                     {:scroll-amount           scroll-amount
                      :expand-header-threshold expand-header-threshold
                      :text-movement-threshold text-movement-threshold})]
    [rn/view {:style style/community-name-and-description}
     [reanimated/view {:style name-styles}
      [quo/text
       {:accessibility-label :community-title
        :weight              :semi-bold
        :size                :heading-1
        :number-of-lines     1}
       community-name]]
     [reanimated/view {:style [{:opacity 0} info-styles]}
      [quo/text
       {:accessibility-label :community-description
        :weight              :regular
        :size                :paragraph-1}
       community-description]]]))

(defn- community-info-tags
  [community-tags info-styles]
  (when (seq community-tags)
    [reanimated/view {:style info-styles}
     [quo/community-tags
      {:tags            community-tags
       :last-item-style style/community-tags-last-item
       :container-style style/community-tags}]]))

(defn- community-info-stats
  [{:keys [members-count active-members-count info-styles]}]
  [reanimated/view {:style [style/community-stats info-styles]}
   [rn/view {:style {:flex-direction :row :column-gap 12}}
    [quo/community-stat
     {:accessibility-label :stats-members-count
      :icon                :i/group
      :value               members-count}]
    [quo/community-stat
     {:accessibility-label :stats-active-count
      :icon                :i/active-members
      :value               active-members-count}]]])

(defn- community-info
  [{:keys [scroll-amount header-height community-id]}]
  (let [theme             (quo.context/use-theme)
        sheet-styles      (worklets/use-sheet-styles
                           {:scroll-amount                scroll-amount
                            :expand-header-threshold      expand-header-threshold
                            :sheet-displacement-threshold sheet-displacement-threshold})
        info-styles       (worklets/use-info-styles
                           {:scroll-amount          scroll-amount
                            :info-opacity-threshold info-opacity-threshold})
        set-header-height (rn/use-callback
                           (fn [e]
                             (let [height (oops/oget e "nativeEvent.layout.height")]
                               (reanimated/set-shared-value header-height (or height 0)))))
        {:keys [community-name description active-members-count tags role-permissions?
                permissions color
                joined?]} (rf/sub [:communities/community-overview community-id])
        members-count     (count (rf/sub [:communities/community-members community-id]))]
    [reanimated/view
     {:style     [(style/community-info theme) sheet-styles]
      :on-layout set-header-height}
     [status-tag
      {:community-id community-id
       :joined?      joined?
       :info-styles  info-styles}]
     [name-and-description
      {:scroll-amount         scroll-amount
       :community-name        community-name
       :community-description description
       :info-styles           info-styles}]
     [community-info-stats
      {:members-count        members-count
       :active-members-count active-members-count
       :info-styles          info-styles}]
     [community-info-tags tags info-styles]
     [join-community
      {:community-id      community-id
       :joined?           joined?
       :tags?             (seq tags)
       :permissions       permissions
       :role-permissions? role-permissions?
       :color             color}]]))

(defn- community-sheet
  [{:keys [community-id scroll-amount set-max-scroll]}]
  (let [header-height (reanimated/use-shared-value 0)]
    [rn/view {:style style/community-sheet-position}
     [community-logo
      {:community-id  community-id
       :scroll-amount scroll-amount}]
     [community-info
      {:scroll-amount scroll-amount
       :header-height header-height
       :community-id  community-id}]
     [channel-listing
      {:community-id   community-id
       :scroll-amount  scroll-amount
       :header-height  header-height
       :set-max-scroll set-max-scroll}]]))

(defn- community-overview
  [community-id collapsed?]
  (let [max-scroll     (reanimated/use-shared-value 0)
        set-max-scroll (rn/use-callback
                        (fn [max-scroll-amount]
                          (reanimated/set-shared-value max-scroll max-scroll-amount)))
        scroll-start   (reanimated/use-shared-value (if collapsed? (- expand-header-threshold) 0))
        scroll-amount  (reanimated/use-shared-value (if collapsed? expand-header-threshold 0))
        on-pan-start   (worklets/on-pan-start scroll-start scroll-amount)
        on-pan-update  (worklets/on-pan-update
                        {:scroll-start        scroll-start
                         :scroll-amount       scroll-amount
                         :max-scroll          max-scroll
                         :expand-header-limit expand-header-limit})
        on-pan-end     (worklets/on-pan-end
                        {:scroll-start            scroll-start
                         :scroll-amount           scroll-amount
                         :max-scroll              max-scroll
                         :expand-header-limit     expand-header-limit
                         :expand-header-threshold expand-header-threshold
                         :snap-header-threshold   snap-header-threshold
                         :animation-duration      300})
        pan-gesture    (-> (gesture/gesture-pan)
                           (gesture/on-start on-pan-start)
                           (gesture/on-update on-pan-update)
                           (gesture/on-end on-pan-end))]
    [gesture/gesture-detector {:gesture pan-gesture}
     [rn/view {:style {:flex 1}}
      [header community-id scroll-amount]
      [community-sheet
       {:community-id   community-id
        :scroll-amount  scroll-amount
        :set-max-scroll set-max-scroll}]]]))

(defn- community-fetching-placeholder
  [id]
  (let [theme     (quo.context/use-theme)
        top-inset (safe-area/get-top)
        fetching? (rf/sub [:communities/fetching-community id])]
    [rn/view
     {:style               (style/fetching-placeholder top-inset)
      :accessibility-label (if fetching?
                             :fetching-community-overview
                             :failed-to-fetch-community-overview)}
     [quo/page-nav
      {:title      (i18n/label :t/community-overview)
       :type       :title
       :text-align :left
       :icon-name  :i/close
       :on-press   events.helper/navigate-back}]
     [quo/empty-state
      {:image           (resources/get-themed-image :cat-in-box theme)
       :description     (when-not fetching? (i18n/label :t/here-is-a-cat-in-a-box-instead))
       :title           (if fetching?
                          (i18n/label :t/fetching-community)
                          (i18n/label :t/failed-to-fetch-community))
       :container-style {:flex 1 :justify-content :center}}]]))

(defn view
  [id]
  (let [community-id (or id (quo.context/use-screen-params))
        community    (rf/sub [:communities/community-overview community-id])
        collapsed?   (:joined? community)]
    [rn/view {:style style/community-overview-container}
     (if community
       [community-overview community-id collapsed?]
       [community-fetching-placeholder community-id])]))
