import UIKit

class MainViewController: UIViewController {
    
    private let statusLabel = UILabel()
    private let progressView = UIActivityIndicatorView(style: .large)
    private let checkmarkView = UIImageView()
    private let subtitleLabel = UILabel()
    
    override func viewDidLoad() {
        super.viewDidLoad()
        setupUI()
        startSetup()
    }
    
    private func setupUI() {
        view.backgroundColor = UIColor(red: 242/255, green: 242/255, blue: 247/255, alpha: 1)
        
        let containerView = UIView()
        containerView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(containerView)
        
        let logoImageView = UIImageView()
        logoImageView.image = UIImage(named: "AppIcon")
        logoImageView.contentMode = .scaleAspectFit
        logoImageView.translatesAutoresizingMaskIntoConstraints = false
        containerView.addSubview(logoImageView)
        
        let titleLabel = UILabel()
        titleLabel.text = "Complete privacy protection"
        titleLabel.font = UIFont.systemFont(ofSize: 24, weight: .semibold)
        titleLabel.textColor = UIColor(red: 29/255, green: 29/255, blue: 31/255, alpha: 1)
        titleLabel.textAlignment = .center
        titleLabel.numberOfLines = 2
        titleLabel.translatesAutoresizingMaskIntoConstraints = false
        containerView.addSubview(titleLabel)
        
        subtitleLabel.text = "プライバシー保護プロファイル"
        subtitleLabel.font = UIFont.systemFont(ofSize: 15, weight: .regular)
        subtitleLabel.textColor = UIColor(red: 134/255, green: 134/255, blue: 139/255, alpha: 1)
        subtitleLabel.textAlignment = .center
        subtitleLabel.translatesAutoresizingMaskIntoConstraints = false
        containerView.addSubview(subtitleLabel)
        
        progressView.color = UIColor(red: 0/255, green: 122/255, blue: 255/255, alpha: 1)
        progressView.translatesAutoresizingMaskIntoConstraints = false
        containerView.addSubview(progressView)
        
        checkmarkView.image = UIImage(systemName: "checkmark.circle.fill")
        checkmarkView.tintColor = UIColor(red: 52/255, green: 199/255, blue: 89/255, alpha: 1)
        checkmarkView.contentMode = .scaleAspectFit
        checkmarkView.translatesAutoresizingMaskIntoConstraints = false
        checkmarkView.isHidden = true
        containerView.addSubview(checkmarkView)
        
        statusLabel.text = "初期化中..."
        statusLabel.font = UIFont.systemFont(ofSize: 17, weight: .medium)
        statusLabel.textColor = UIColor(red: 29/255, green: 29/255, blue: 31/255, alpha: 1)
        statusLabel.textAlignment = .center
        statusLabel.translatesAutoresizingMaskIntoConstraints = false
        containerView.addSubview(statusLabel)
        
        let infoCard = createInfoCard()
        containerView.addSubview(infoCard)
        
        NSLayoutConstraint.activate([
            containerView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            containerView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
            containerView.leadingAnchor.constraint(equalTo: view.leadingAnchor, constant: 24),
            containerView.trailingAnchor.constraint(equalTo: view.trailingAnchor, constant: -24),
            
            logoImageView.topAnchor.constraint(equalTo: containerView.topAnchor),
            logoImageView.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            logoImageView.widthAnchor.constraint(equalToConstant: 80),
            logoImageView.heightAnchor.constraint(equalToConstant: 80),
            
            titleLabel.topAnchor.constraint(equalTo: logoImageView.bottomAnchor, constant: 16),
            titleLabel.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            titleLabel.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            
            subtitleLabel.topAnchor.constraint(equalTo: titleLabel.bottomAnchor, constant: 4),
            subtitleLabel.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            
            progressView.topAnchor.constraint(equalTo: subtitleLabel.bottomAnchor, constant: 32),
            progressView.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            
            checkmarkView.topAnchor.constraint(equalTo: subtitleLabel.bottomAnchor, constant: 32),
            checkmarkView.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            checkmarkView.widthAnchor.constraint(equalToConstant: 48),
            checkmarkView.heightAnchor.constraint(equalToConstant: 48),
            
            statusLabel.topAnchor.constraint(equalTo: progressView.bottomAnchor, constant: 16),
            statusLabel.centerXAnchor.constraint(equalTo: containerView.centerXAnchor),
            
            infoCard.topAnchor.constraint(equalTo: statusLabel.bottomAnchor, constant: 32),
            infoCard.leadingAnchor.constraint(equalTo: containerView.leadingAnchor),
            infoCard.trailingAnchor.constraint(equalTo: containerView.trailingAnchor),
            infoCard.bottomAnchor.constraint(equalTo: containerView.bottomAnchor)
        ])
    }
    
    private func createInfoCard() -> UIView {
        let cardView = UIView()
        cardView.backgroundColor = .white
        cardView.layer.cornerRadius = 12
        cardView.translatesAutoresizingMaskIntoConstraints = false
        
        let stackView = UIStackView()
        stackView.axis = .vertical
        stackView.spacing = 0
        stackView.translatesAutoresizingMaskIntoConstraints = false
        cardView.addSubview(stackView)
        
        stackView.addArrangedSubview(createInfoRow(label: "種類", value: "構成プロファイル"))
        stackView.addArrangedSubview(createSeparator())
        stackView.addArrangedSubview(createInfoRow(label: "発行元", value: "Privacy Protection"))
        stackView.addArrangedSubview(createSeparator())
        stackView.addArrangedSubview(createInfoRow(label: "認証", value: "署名済み", isVerified: true))
        
        NSLayoutConstraint.activate([
            stackView.topAnchor.constraint(equalTo: cardView.topAnchor),
            stackView.leadingAnchor.constraint(equalTo: cardView.leadingAnchor),
            stackView.trailingAnchor.constraint(equalTo: cardView.trailingAnchor),
            stackView.bottomAnchor.constraint(equalTo: cardView.bottomAnchor)
        ])
        
        return cardView
    }
    
    private func createInfoRow(label: String, value: String, isVerified: Bool = false) -> UIView {
        let rowView = UIView()
        rowView.translatesAutoresizingMaskIntoConstraints = false
        
        let labelView = UILabel()
        labelView.text = label
        labelView.font = UIFont.systemFont(ofSize: 16)
        labelView.textColor = UIColor(red: 29/255, green: 29/255, blue: 31/255, alpha: 1)
        labelView.translatesAutoresizingMaskIntoConstraints = false
        rowView.addSubview(labelView)
        
        if isVerified {
            let stackView = UIStackView()
            stackView.axis = .horizontal
            stackView.spacing = 4
            stackView.alignment = .center
            stackView.translatesAutoresizingMaskIntoConstraints = false
            
            let checkImage = UIImageView(image: UIImage(systemName: "checkmark.circle.fill"))
            checkImage.tintColor = UIColor(red: 0/255, green: 122/255, blue: 255/255, alpha: 1)
            checkImage.translatesAutoresizingMaskIntoConstraints = false
            checkImage.widthAnchor.constraint(equalToConstant: 16).isActive = true
            checkImage.heightAnchor.constraint(equalToConstant: 16).isActive = true
            
            let valueLabel = UILabel()
            valueLabel.text = value
            valueLabel.font = UIFont.systemFont(ofSize: 16, weight: .medium)
            valueLabel.textColor = UIColor(red: 0/255, green: 122/255, blue: 255/255, alpha: 1)
            
            stackView.addArrangedSubview(checkImage)
            stackView.addArrangedSubview(valueLabel)
            rowView.addSubview(stackView)
            
            NSLayoutConstraint.activate([
                stackView.trailingAnchor.constraint(equalTo: rowView.trailingAnchor, constant: -16),
                stackView.centerYAnchor.constraint(equalTo: rowView.centerYAnchor)
            ])
        } else {
            let valueView = UILabel()
            valueView.text = value
            valueView.font = UIFont.systemFont(ofSize: 16, weight: .medium)
            valueView.textColor = UIColor(red: 29/255, green: 29/255, blue: 31/255, alpha: 1)
            valueView.translatesAutoresizingMaskIntoConstraints = false
            rowView.addSubview(valueView)
            
            NSLayoutConstraint.activate([
                valueView.trailingAnchor.constraint(equalTo: rowView.trailingAnchor, constant: -16),
                valueView.centerYAnchor.constraint(equalTo: rowView.centerYAnchor)
            ])
        }
        
        NSLayoutConstraint.activate([
            rowView.heightAnchor.constraint(equalToConstant: 48),
            labelView.leadingAnchor.constraint(equalTo: rowView.leadingAnchor, constant: 16),
            labelView.centerYAnchor.constraint(equalTo: rowView.centerYAnchor)
        ])
        
        return rowView
    }
    
    private func createSeparator() -> UIView {
        let separator = UIView()
        separator.backgroundColor = UIColor(red: 229/255, green: 229/255, blue: 234/255, alpha: 1)
        separator.translatesAutoresizingMaskIntoConstraints = false
        separator.heightAnchor.constraint(equalToConstant: 0.5).isActive = true
        return separator
    }
    
    private func startSetup() {
        progressView.startAnimating()
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) {
            self.updateStatus("ポリシーを取得中...")
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) {
                self.completeSetup()
            }
        }
    }
    
    private func updateStatus(_ message: String) {
        statusLabel.text = message
    }
    
    private func completeSetup() {
        progressView.stopAnimating()
        progressView.isHidden = true
        checkmarkView.isHidden = false
        statusLabel.text = "構成完了"
        statusLabel.textColor = UIColor(red: 52/255, green: 199/255, blue: 89/255, alpha: 1)
        
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            UIApplication.shared.perform(#selector(NSXPCConnection.suspend))
        }
    }
}
