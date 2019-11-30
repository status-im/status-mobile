(ns fiddle.snippets)

(defmacro code-snippet [code & [white?]]
  (let [show? (gensym 'show)]
    `[(fn []
        (let [~show? (reagent/atom false)]
          (fn []
            [react/view
             (when @~show?
               [react/text {:style {:font-family "monospace" :background-color :black :color "#44d058"
                                    :max-width 400 }}
                (with-out-str (cljs.pprint/pprint '~code))])
             [react/view {:align-items :flex-end :background-color (when-not ~white? colors/gray-lighter)}
              [react/touchable-highlight {:on-press #(swap! ~show? not)}
               [react/text {:style {:font-size 14 :color :blue :padding 3}} (if @~show? "Hide" "Code")]]]
             ~code])))]))