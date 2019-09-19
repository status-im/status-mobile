(ns status-im.ui.components.large-toolbar.view
  (:require [reagent.core :as reagent]
            [cljs-bean.core :refer [->clj ->js]]
            [status-im.ui.components.list.views :as list.views]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.large-toolbar.styles :as styles]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.animation :as animation])
  (:require-macros [status-im.utils.views :as views]))

;; header-in-toolbar - component - small header in toolbar
;; nav-item          - component/nil - if nav-item like back button is needed, else nil
;; action-items      - status-im.ui.components.toolbar.view/actions 
(defn minimized-toolbar [header-in-toolbar nav-item action-items anim-opacity]
  (let [has-nav? (boolean nav-item)]
    [toolbar/toolbar
     {:transparent? true
      :style        styles/minimized-toolbar}
     nav-item
     [react/animated-view
      {:style (cond-> (styles/animated-content-wrapper anim-opacity)
                (false? has-nav?)
                (assoc :margin-left -40 :margin-right 40))}
      header-in-toolbar]
     action-items]))

;; header   - component that serves as large header without any top/bottom padding
;;            top(4px high) and bottom(16px high and with border) padding
;;            are assumed to be constant
;;            this is wrapped with padding components and merged with content
;; content  - vector - of the rest(from header) of the list components
;;            wrapped header and content form the data prop of flat-list
;; list-ref - atom - a reference to flat-list for the purpose of invoking its
;;            methods
;; scroll-y - animated value tracking the y scoll of the main content in flat-list-view
(views/defview flat-list-with-large-header [header content list-ref scroll-y]
  (views/letsubs [window-width [:dimensions/window-width]]
    (let [header-top-padding [react/view {:height 4}]
          ;; header bottom padding with border-bottom
          ;; fades out as it approaches toolbar shadow
          header-bottom      [react/animated-view
                              {:style (styles/flat-list-with-large-header-bottom scroll-y)}]
          wrapped-data       (into [header-top-padding header header-bottom] content)]
      [react/view {:flex 1}
       ;; toolbar shadow
       [react/animated-view
        {:style (styles/flat-list-with-large-header-shadow window-width scroll-y)}]

       [list.views/flat-list
        {:style                          styles/flat-list
         :data                           wrapped-data
         :initial-num-to-render          3
         :ref                            #(when % (reset! list-ref (.getNode %)))
         :render-fn                      list.views/flat-list-generic-render-fn
         :key-fn                         (fn [item idx] (str idx))
         :scrollEventThrottle            16
         :on-scroll                      (animation/event
                                          [{:nativeEvent {:contentOffset {:y scroll-y}}}]
                                          {:useNativeDriver true})
         :keyboard-should-persist-taps   :handled}
        {:animated? true}]])))

(defn generate-view
  "main function which generates views.
   - it will generate and return back:
   - minimized-toolbar
   - flat-list-with-large-header"
  [header-in-toolbar nav-item toolbar-action-items header content list-ref]
  (let [to-hide  (reagent/atom false)
        anim-opacity (animation/create-value 0)
        scroll-y (animation/create-value 0)]
    (animation/add-listener scroll-y (fn [anim]
                                       (cond
                                         (and (>= (.-value anim) 40) (not @to-hide))
                                         (animation/start
                                          (styles/minimized-toolbar-fade-in anim-opacity)
                                          #(reset! to-hide true))

                                         (and (< (.-value anim) 40) @to-hide)
                                         (animation/start
                                          (styles/minimized-toolbar-fade-out anim-opacity)
                                          #(reset! to-hide false)))))
    {:minimized-toolbar [minimized-toolbar header-in-toolbar nav-item toolbar-action-items anim-opacity]
     :content-with-header [flat-list-with-large-header header content list-ref scroll-y]}))