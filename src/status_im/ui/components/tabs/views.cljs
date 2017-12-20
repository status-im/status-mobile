(ns status-im.ui.components.tabs.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as styles]
            [status-im.ui.components.tabs.styles :as tabs.styles])
  (:require-macros [status-im.utils.views :refer [defview]]))

(defview tab [index content tab-style on-press active?]
  [react/touchable-highlight {:style (when tab-style
                                       (tab-style active?))
                              :disabled active?
                              :on-press #(on-press index)}
   [react/view
    [content active?]]])

(defn tabs [tabs-container-style indexed-tabs tab-style on-press is-current-tab?]
  [react/view {:style tabs-container-style}
   (for [[index {:keys [content view-id]}] indexed-tabs]
     ^{:key index} [tab index content tab-style on-press (is-current-tab? view-id)])])

(defn swipable-tabs [tabs-list current-tab show-tabs?
                     {:keys [bottom-tabs? navigation-event main-container-style tabs-container-style tab-style]
                      :or {bottom-tabs          false
                           navigation-event     :navigate-to
                           tabs-container-style tabs.styles/tabs-container
                           tab-style            tabs.styles/tab}}]
  (let [swiper        (atom nil)
        indexed-tabs  (map-indexed vector tabs-list)
        tab->index    (reduce (fn [acc [index tab]]
                                (assoc acc (:view-id tab) index))
                              {}
                              indexed-tabs)
        index->tab    (clojure.set/map-invert tab->index)
        get-tab-index #(get tab->index % 0)]
    (fn [tabs-list current-tab show-tabs?]
      (let [current-tab-index (get-tab-index current-tab)
            on-press          (fn [index]
                                (.scrollBy @swiper (- index current-tab-index)))
            is-current-tab?   (fn [view-id]
                                (= (get-tab-index view-id) current-tab-index))]
        [react/view styles/main-container
         (when (and (not bottom-tabs?)
                    show-tabs?)
           [tabs tabs-container-style indexed-tabs tab-style on-press is-current-tab?])
         [react/swiper {:loop             false
                        :shows-pagination false
                        :index            (get-tab-index current-tab)
                        :ref              #(reset! swiper %)
                        :on-index-changed #(re-frame/dispatch [navigation-event (index->tab %)])}
          (for [[index {:keys [screen view-id]}] indexed-tabs]
            ^{:key index} [screen (is-current-tab? view-id)])]
         (when (and bottom-tabs?
                    show-tabs?)
           [tabs tabs-container-style indexed-tabs tab-style on-press is-current-tab?])]))))
