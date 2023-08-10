(ns quo2.components.buttons.dynamic-button.style)

(defn container
  [type]
  {:margin-top    6
   :margin-bottom 6
   :margin-left   (case type
                    :jump-to           0
                    :mention           8
                    :notification-down 2
                    :notification-up   2
                    :search-with-label 8
                    :search            6
                    :scroll-to-bottom  6
                    nil)
   :margin-right  (case type
                    :jump-to           8
                    :mention           2
                    :notification-down 8
                    :notification-up   8
                    :search-with-label 4
                    :search            6
                    :scroll-to-bottom  6
                    nil)})

(defn text
  [type]
  {:margin-top    2.5
   :margin-bottom 3.5
   :margin-left   (case type
                    :jump-to           8
                    :mention           0
                    :notification-down 8
                    :notification-up   8
                    :search-with-label 0
                    nil)
   :margin-right  (case type
                    :jump-to           0
                    :mention           8
                    :notification-down 0
                    :notification-up   0
                    :search-with-label 8
                    nil)})
