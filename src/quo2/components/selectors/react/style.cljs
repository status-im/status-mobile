(ns quo2.components.selectors.react.style)

(def container
  {:flex-direction  :row
   :justify-content :flex-start
   :flex            1
   :flex-wrap       :wrap
   ;; FIXME: after upgrading to >rn71.0, use :row-gap to
   ;; add the gap between rows and remove the artificial
   ;; bottom margins. Needed due to the margin between
   ;; reaction rows (if more than 1), which shouldn't
   ;; affect the component's container.
   ;; :row-gap 6
   :margin-bottom   -6})

(def reaction-container
  ;; FIXME: remove bottom margin after upgrading to >rn71.0
  {:margin-bottom 6
   :margin-right  6})
