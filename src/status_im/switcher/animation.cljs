(ns status-im.switcher.animation
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [status-im.switcher.constants :as constants]
            [status-im.ui.components.animation :as anim]))

(def bottom-tabs-opacity (anim/create-value 1))
(def bottom-tabs-position (anim/create-value 0))

;; TODO(parvesh): Use 300, after using dispatch-later for opening card(otherwise pending animation issue)
;; or OnAnimationEnd
(def layout-animation #js {:duration 250
                           :create   #js {:type     (:ease-in-ease-out rn/layout-animation-types)
                                          :property (:scale-xy rn/layout-animation-properties)}
                           :update   #js {:type     (:ease-in-ease-out rn/layout-animation-types)
                                          :property (:scale-xy rn/layout-animation-properties)}
                           :delete   #js {:type     (:ease-in-ease-out rn/layout-animation-types)
                                          :property (:scale-xy rn/layout-animation-properties)}})

(defn animate-layout [show? anim-values]
  (let [{:keys [width height]} (constants/dimensions)
        target-radius          (- (max width height)
                                  constants/switcher-button-radius)]
    (rn/configure-next layout-animation)
    (reset! (:switcher-screen-radius anim-values) (if show? target-radius 1))
    (reagent/flush)))

(defn timing-animation [property toValue]
  (anim/timing property {:toValue         toValue
                         :duration        300
                         :useNativeDriver true}))

(defn animate-components [show? view-id anim-values]
  (anim/start
   (anim/parallel
    (into
     [(timing-animation (:switcher-button-opacity anim-values) (if show? 0 1))
      (timing-animation (:switcher-close-button-icon-opacity anim-values) (if show? 1 0))
      (timing-animation (:switcher-close-button-background-opacity anim-values) (if show? 0.2 0))]
     (when (= view-id :home-stack)
       [(timing-animation bottom-tabs-opacity (if show? 0 1))
        (timing-animation bottom-tabs-position (if show? (constants/bottom-tabs-height) 0))])))))

(defn animate [show? view-id anim-values]
  (reagent/flush)
  (animate-layout show? anim-values)
  (animate-components show? view-id anim-values))
