User 方法
+ register(username: String, email: String, password: String): void
+ login(username: String, password: String): boolean
+ logout(): void
+ updateProfile(email: String, newPassword: String): void
+ browseGames(): List<Game>
+ searchGames(): List<Game>
+ viewGameDetails(): Game
+ addToWishlist(game: Game): void
+ checkIfGameInList(game: Game): boolean
+ purchaseGame(game: Game): void
+ getMyGames(): List<Game>
+ downloadGame(game: Game): void
+ checkDownloadCondition(): boolean
+ recommendGame(game: Game): void
+ viewOrderHistory(): List <Transaction>
+ publishPost(): Post
+ commentInPost(comment: String): void
+ sharePost(post: Post): void
+ followPoster(user: RegisteredUser): void 
+ writeReview(game: Game, rating: int, comment: String): void
+ viewReview(): View
+ rateReview(review: Review, rating: int): void
+ subscribe(): void
+ receiveNotifications(): Notification
+ checkSubscribe(): boolean


Post
+ postID: String
+ content: String
+ game: Game
+ dateGenerated: Date3

+ generate(): Post
+ view(): String
