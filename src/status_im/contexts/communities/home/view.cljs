(ns status-im.contexts.communities.home.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]
    [status-im.common.home.banner.view :as common.banner]
    [status-im.common.home.empty-state.view :as common.empty-state]
    [status-im.common.home.header-spacing.view :as common.header-spacing]
    [status-im.common.resources :as resources]
    [status-im.config :as config]
    [status-im.contexts.communities.actions.community-options.view :as options]
    [status-im.contexts.communities.actions.home-plus.view :as actions.home-plus]
    [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
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
      :on-press            #(debounce/throttle-and-dispatch [:communities/navigate-to-community-overview
                                                             id]
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
   {:label               (i18n/label :t/communities)
    :handler             (when config/fast-create-community-enabled?
                           #(rf/dispatch [:show-bottom-sheet {:content actions.home-plus/view}]))
    :accessibility-label :new-communities-button}
   :card-props
   {:on-press            #(rf/dispatch [:navigate-to :discover-communities])
    :title               (i18n/label :t/discover)
    :description         (i18n/label :t/favorite-communities)
    :banner              (resources/get-image :discover)
    :accessibility-label :communities-home-discover-card}})

(defn- f-view-internal
  [{:keys [theme]}]
  (let [flat-list-ref     (atom nil)
        set-flat-list-ref #(reset! flat-list-ref %)]
    (fn []
      (let [customization-color             (rf/sub [:profile/customization-color])
            selected-tab                    (or (rf/sub [:communities/selected-tab]) :joined)
            {:keys [joined pending opened]} (rf/sub [:communities/grouped-by-status])
            selected-items                  (case selected-tab
                                              :joined  joined
                                              :pending pending
                                              :opened  opened)
            scroll-shared-value             (reanimated/use-shared-value 0)]
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
             :data                              (sort-by (fn [{:keys [last-opened-at joined-at]}]
                                                           (or last-opened-at joined-at))
                                                         #(compare %2 %1)
                                                         selected-items)
             :scroll-event-throttle             8
             :content-container-style           {:padding-bottom
                                                 jump-to.constants/floating-shell-button-height}
             :on-scroll                         #(common.banner/set-scroll-shared-value
                                                  {:scroll-input (oops/oget
                                                                  %
                                                                  "nativeEvent.contentOffset.y")
                                                   :shared-value scroll-shared-value})}])
         [:f> common.banner/animated-banner
          {:content             banner-data
           :customization-color customization-color
           :scroll-ref          flat-list-ref
           :tabs                tabs-data
           :selected-tab        selected-tab
           :on-tab-change       (fn [tab] (rf/dispatch [:communities/select-tab tab]))
           :scroll-shared-value scroll-shared-value}]]))))

(defn- internal-communities-home-view
  [params]
  [:f> f-view-internal params])

(def view (quo.theme/with-theme internal-communities-home-view))
