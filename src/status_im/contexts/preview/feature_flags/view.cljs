(ns status-im.contexts.preview.feature-flags.view
  (:require
    [quo.core :as quo]
    [re-frame.core :as rf]
    [react-native.core :as rn]
    [status-im.feature-flags :as ff]))

(defn view
  []
  [rn/view {:flex 1}
   [quo/page-nav
    {:type       :title
     :text-align :left
     :title      "Features Flags"
     :icon-name  :i/arrow-left
     :on-press   #(rf/dispatch [:navigate-back])}]
   (for [[context-name context-flags] (ff/feature-flags)]
     ^{:key (str context-name)}
     [rn/view
      {:flex        1
       :margin-left 20}
      [quo/text {:color :black} (name context-name)]

      (for [[flag _v] context-flags]
        ^{:key (str context-name flag)}

        [rn/view {:flex-direction :row}
         (prn (ff/get-flag context-name flag))
         [quo/selectors
          {:type            :toggle
           :checked?        (ff/get-flag context-name flag)
           :container-style {:margin-right 8}
           :on-change       #(ff/update-flag context-name flag)}]
         [quo/text {:color :black} (name flag)]])])])
