(ns status-im.components.tabs.tab
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.components.react :refer [view
                                                animated-view
                                                text
                                                image
                                                touchable-highlight]]
            [reagent.core :as r]
            [status-im.components.tabs.styles :as st]
            [status-im.components.animation :as anim]))

(defn animation-logic [val to-value]
  (fn []
    (anim/start (anim/spring val {:toValue to-value
                                  :tension 40}))))

(defview tab [_]
  (let [icon-anim-value         (anim/create-value 0)
        text-anim-value         (anim/create-value 0)
        icon-reverse-anim-value (anim/create-value 0)
        text-reverse-anim-value (anim/create-value 0)
        on-update               (comp (animation-logic icon-anim-value 0)
                                      (animation-logic text-anim-value 0)
                                      (animation-logic icon-reverse-anim-value 0)
                                      (animation-logic text-reverse-anim-value 30))]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :component-will-receive-props
       (fn []
         (anim/set-value icon-anim-value 8)
         (anim/set-value text-anim-value 30)
         (anim/set-value icon-reverse-anim-value -8)
         (anim/set-value text-reverse-anim-value -8))
       :reagent-render
       (fn [{:keys [view-id title icon selected-view-id prev-view-id]}]
         [touchable-highlight {:style    st/tab
                               :disabled (= view-id selected-view-id)
                               :onPress  #(dispatch [:navigate-to-tab view-id])}
          [view {:style (st/tab-container (= selected-view-id view-id))}
           [animated-view {:style (st/animated-offset (cond
                                                        (= selected-view-id view-id) icon-anim-value
                                                        (= prev-view-id view-id) icon-reverse-anim-value
                                                        :else 0))}
            [image {:source {:uri icon}
                    :style  st/tab-icon}]]
           [animated-view {:style (st/animated-offset (cond
                                                        (= selected-view-id view-id) text-anim-value
                                                        (= prev-view-id view-id) text-reverse-anim-value
                                                        :else 0))}
            [text {:style st/tab-title}
             (if (or (= selected-view-id view-id)
                       (= prev-view-id view-id))
               title
               " ")]]]])})))
