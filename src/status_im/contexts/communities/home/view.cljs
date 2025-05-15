(ns status-im.contexts.communities.home.view
  (:require
    [oops.core :as oops]
    [quo.context :as quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.common.home.banner.view :as common.banner]
    [status-im.common.home.empty-state.view :as common.empty-state]
    [status-im.common.home.header-spacing.view :as common.header-spacing]
    [status-im.common.resources :as resources]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.communities.actions.community-options.view :as options]
    [status-im.contexts.shell.constants :as shell.constants]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.number]
    [utils.re-frame :as rf]))

(defn item-render
  [{:keys [id] :as item}]
  (let [unviewed-counts     (rf/sub [:communities/unviewed-counts id])
        customization-color (rf/sub [:profile/customization-color])
        item                (merge item unviewed-counts)]
    [quo/communities-membership-list-item
     {:customization-color customization-color
      :style               {:padding-horizontal 20}
      :on-press            #(debounce/throttle-and-dispatch
                             [:communities/navigate-to-community-overview id]
                             500)
      :on-long-press       #(rf/dispatch
                             [:show-bottom-sheet
                              {:content       (fn []
                                                [options/community-options-bottom-sheet id])
                               :selected-item (fn []
                                                [quo/communities-membership-list-item {} true item])}])}
     false
     item]))

(def tabs-data
  [{:id :joined :label (i18n/label :t/joined) :accessibility-label :joined-tab}
   {:id :pending :label (i18n/label :t/pending) :accessibility-label :pending-tab}
   {:id :opened :label (i18n/label :t/opened) :accessibility-label :opened-tab}])

(defn- community-creation-options-testing
  []
  [rn/view {:style {:padding-vertical 12 :row-gap 12}}
   [quo/divider-line]
   [rn/view
    [quo/action-drawer
     [[{:icon                :i/communities
        :accessibility-label :create-closed-community
        :label               "Create closed community (only for testing)"
        :on-press            #(rf/dispatch [:fast-create-community/create-closed-community])}
       {:icon                :i/communities
        :accessibility-label :create-open-community
        :label               "Create open community (only for testing)"
        :on-press            #(rf/dispatch [:fast-create-community/create-open-community])}
       {:icon                :i/communities
        :accessibility-label :create-token-gated-community
        :label               "Create token-gated community (only for testing)"
        :on-press            #(rf/dispatch
                               [:fast-create-community/create-token-gated-community])}]]]]])

(defn- open-learn-more-link
  []
  (rf/dispatch [:hide-bottom-sheet])
  (rf/dispatch
   [:browser.ui/open-url constants/create-community-help-url]))

(defn- hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn- create-community-sheet
  []
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top {:title (i18n/label :t/want-to-create-community)}]
     [quo/text {:style {:padding-horizontal 20 :padding-bottom 12}}
      (i18n/label :t/communities-only-available-in-desktop)]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/learn-more)
       :button-one-props {:disabled?           false
                          :customization-color customization-color
                          :on-press            open-learn-more-link
                          :icon-right          :i/external}
       :button-two-label (i18n/label :t/maybe-later)
       :button-two-props {:type     :grey
                          :on-press hide-bottom-sheet}}]
     (when config/fast-create-community-enabled?
       [community-creation-options-testing])]))

(defn empty-state-content
  [theme]
  {:joined
   {:title       (i18n/label :t/no-communities)
    :description [:<>
                  [rn/text {:style {:text-decoration-line :line-through}}
                   (i18n/label :t/no-communities-description-strikethrough)]
                  " "
                  (i18n/label :t/no-communities-description)]
    :image       (resources/get-themed-image :no-communities theme)}
   :pending
   {:title       (i18n/label :t/no-pending-communities)
    :description (i18n/label :t/no-pending-communities-description)
    :image       (resources/get-themed-image :no-pending-communities theme)}
   :opened
   {:title       (i18n/label :t/no-opened-communities)
    :description (i18n/label :t/no-opened-communities-description)
    :image       (resources/get-themed-image :no-opened-communities theme)}})

(def ^:private banner-data
  {:title-props
   {:beta?               true
    :label               (i18n/label :t/communities)
    :handler             #(rf/dispatch [:show-bottom-sheet {:content create-community-sheet}])
    :accessibility-label :new-communities-button}
   :card-props
   {:on-press            #(rf/dispatch [:navigate-to :screen/discover-communities])
    :title               (i18n/label :t/discover)
    :description         (i18n/label :t/favorite-communities)
    :banner              (resources/get-image :discover)
    :accessibility-label :communities-home-discover-card}})

(defn on-tab-change
  [tab]
  (rf/dispatch [:communities/select-tab tab]))

(defn view
  []
  (let [flat-list-ref                   (rn/use-ref-atom nil)
        set-flat-list-ref               (rn/use-callback #(reset! flat-list-ref %))
        theme                           (quo.context/use-theme)
        customization-color             (rf/sub [:profile/customization-color])
        selected-tab                    (or (rf/sub [:communities/selected-tab]) :joined)
        {:keys [joined pending opened]} (rf/sub [:communities/grouped-by-status])
        selected-items                  (case selected-tab
                                          :joined  joined
                                          :pending pending
                                          :opened  opened)
        scroll-shared-value             (reanimated/use-shared-value 0)
        on-scroll                       (rn/use-callback
                                         (fn [event]
                                           (common.banner/set-scroll-shared-value
                                            {:scroll-input (oops/oget event
                                                                      "nativeEvent.contentOffset.y")
                                             :shared-value scroll-shared-value})))]
    [:<>
     (if (empty? selected-items)
       [common.empty-state/view
        {:selected-tab selected-tab
         :tab->content (empty-state-content theme)}]
       [reanimated/flat-list
        {:ref                               set-flat-list-ref
         :key-fn                            :id
         :content-inset-adjustment-behavior :never
         :header                            [common.header-spacing/view]
         :render-fn                         item-render
         :style                             {:margin-top -1}
         :data                              selected-items
         :scroll-event-throttle             8
         :content-container-style           {:padding-bottom
                                             shell.constants/floating-shell-button-height}
         :on-scroll                         on-scroll}])
     [common.banner/animated-banner
      {:content             banner-data
       :customization-color customization-color
       :scroll-ref          flat-list-ref
       :tabs                tabs-data
       :selected-tab        selected-tab
       :on-tab-change       on-tab-change
       :scroll-shared-value scroll-shared-value}]]))
