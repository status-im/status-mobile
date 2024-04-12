(ns status-im.contexts.preview.feature-flags.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [re-frame.core :as rf]
    [react-native.core :as rn]
    [status-im.contexts.preview.feature-flags.style :as style]
    [status-im.feature-flags :as ff]))

(defn view
  []
  [rn/view {:style {:flex 1}}
   [quo/page-nav
    {:type       :title
     :text-align :left
     :title      "Features Flags"
     :icon-name  :i/arrow-left
     :on-press   #(rf/dispatch [:navigate-back])
     :right-side [{:icon-name :i/rotate
                   :on-press  #(ff/reset-flags)}]}]
   (doall
    (for [context-name ff/feature-flags-categories
          :let         [context-flags (filter (fn [[k]]
                                                (string/includes? (str k) context-name))
                                              (ff/feature-flags))]]
      ^{:key (str context-name)}
      [rn/view {:style style/container}
       [quo/text
        context-name]
       (doall
        (for [i    (range (count context-flags))
              :let [[flag] (nth context-flags i)]]
          ^{:key (str context-name flag i)}
          [rn/view {:style {:flex-direction :row}}
           [quo/selectors
            {:type            :toggle
             :checked?        (ff/enabled? flag)
             :container-style {:margin-right 8}
             :on-change       #(ff/toggle flag)}]
           [quo/text (second (string/split (name flag) "."))]]))]))])
