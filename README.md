Hello Paytm, 
So I made a small error when setting up this project. I did most of my development over at https://github.com/Alvtag/currencyConverter/; I've migrated the code over to this repo, but that means the git history got squelched. The linked project above has the development git history.

I had some problems with INSTALL_FAILED_INVALID_APK; if so disable Instant Run and it should be fine
stackoverflow.com/questions/34805780/error-installing-split-apks-com-android-ddmlib-installexception-failed-to-fina
https://code.google.com/p/android/issues/detail?id=227610

Some of comments:
- features I debated including (but didn't, since we all know feature creep is terrible):
  - button for swapping the input and output currencies
  - persisting exchange rates data across app sessions (i.e. offline mode)
- when a rate is loaded, its inverse (e.g. USD->CAD/ CAD->USD) is calculated. this could save the use a network call if he swaps between 2 currencies.
- not too much work was put into Volley wrappers, an expansion to the app would benefit from more structure around this area.
- model/presenter classes have test coverage.
    - missing test for VolleyWrapper.getRates() due to
      ```java.lang.IllegalStateException: Failed to transform class with name com.android.volley.toolbox.Volley.
      Reason: cannot find org.apache.http.client.HttpClient```
      this prevented me from powerMockito'ing Volley, but definitely worth researching in the future.

Logic choices:
- input value is stored internally in a stringBuilder. this allows me to easily append/remove digits from the end.
  I have arbitrary precision while the value is stored in the stringBuilder.
- BigDecimals are used for all arithmetic
- converterPresenter.formatNumber(long cents) : I chose to have the signature take LONG instead of String because
  It felt more natural for the caller to figure out how many cents is needed, then for formatNumber to change that to
  dollars.cents. I think this keeps formaNumber lightweight.

Design:
1) MVP to allow easy testing, please see http://imgur.com/a/ioBGN
2) data binding library on XML layer. saves so much boilerplate.

Future steps:
1) convert to MVVM, change the variables in ConverterPresenter to ObservableFields,
 and have ConverterActivity bind to them. remove the interface from ConverterActivity.

~Alvin Fong


#Mobile App Developer Coding Challenge

## Goal:

#### Develop a currency conversion app that allows a user to convert an input value by any of the supplied rates.

- [ ] Fork this repo. Keep it public until we have been able to review it.
- [ ] Android: _Java/Kotlin_ | iOS: _Swift_
- [ ] exchange rates must be fetched from: http://fixer.io/  
- [ ] rates should be persisted locally and refreshed no more frequently than every 30 minutes
- [ ] user must be able to select the input currency from the list of supplied values

### Evaluation:
- [ ] App operates as asked
- [ ] No crashes or bugs
- [ ] SOLID principles
- [ ] Code is understandable and maintainable

UI Suggestion: Input field with a drop-down currency selector, and a list/grid of converted values below.

![UI Suggested Wireframe](ui_suggestion.png)
