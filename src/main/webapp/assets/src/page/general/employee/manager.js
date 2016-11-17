define(function (require, exports, module) {
	var $ = require('jquery'), _ = require('underscore'), Backbone = require('backbone');
	var template = require('template'), confirm = require('component/Confirm'), alert = require('component/Alert');
	require('jquery-util');
	require('bootstrap');
	require('ztree');
	require('plupload');
	var loader = require('component/loader');
	// require.async('ztree.exedit');
	require('jquery-validate');

	jQuery.validator.addMethod('mobile', function (value, element) {
		var patt = /^[1][3,4,5,7,8][0-9]{9}$/;
		return this.optional(element) || patt.test(value);
	}, '手机号码格式不正确');

	var urls = {
		getDepts: CONTEXT_PATH + '/users/getAllDept.do',
		getDeptUsers: CONTEXT_PATH + '/users/getUserByDeptId.do',
		// getAllDeptOrgId: CONTEXT_PATH +
		// '/../ydsp/thirdInterface/getAllDeptOrgId',
		// TODO
		import: CONTEXT_PATH + '/user/userImport.do',
		saveUser: CONTEXT_PATH + '/thirdInterface/saveUser.do',
		updateUser: CONTEXT_PATH + '/thirdInterface/updateUser.do',
		deleteUser: CONTEXT_PATH + '/thirdInterface/deleteUser.do',
		updateDeptByUserId: CONTEXT_PATH
		+ '/thirdInterface/updateDeptByUserId.do',
		// deleteUser: CONTEXT_PATH + '/approval/deleteUser.htm',
		saveDept: CONTEXT_PATH + '/thirdInterface/saveDept.do',
		updateDept: CONTEXT_PATH + '/thirdInterface/updateDept.do',
		deleteDept: CONTEXT_PATH + '/thirdInterface/deleteDept.do'
	};

	var globalShowCount = 3;
	var getPageList = function (pageNo, pageCount, showCount) {
		var pages = [];
		if (pageCount <= showCount) {
			var i = 1;
			while (i <= pageCount) {
				pages.push(i++);
			}
		} else {
			var radius = Math.floor(showCount / 2), upLimit = pageNo + radius, downLimit = pageNo
				- radius;
			while (upLimit > pageCount) {
				upLimit--;
				downLimit--;
			}
			while (downLimit < 1) {
				upLimit++;
				downLimit++;
			}
			if (upLimit >= pageCount) {
				upLimit = pageCount - 1;
			}
			if (downLimit <= 1) {
				downLimit = 2;
			}

			pages.push(1);
			while (downLimit <= upLimit) {
				pages.push(downLimit++);
			}
			pages.push(pageCount);
		}
		return pages;
	};
	var PageModel = Backbone.Model.extend({
		defaults: {
			pageNum: 1,
			pageSize: 10,
			total: 0,
			pages: 1
		}
	});
	var pageRender = template($('#tmpl-pagination').html());
	var PageView = Backbone.View.extend({
		template: pageRender,
		events: {
			'click [data-do="pagechange"]': 'pageChange',
			'click [data-do="first"]': 'pageFirst',
			'click [data-do="prev"]': 'pagePrev',
			'click [data-do="next"]': 'pageNext',
			'click [data-do="last"]': 'pageLast',
		},
		initialize: function (options) {
			this.query = options.query;
			this.listenTo(this.model, 'response', this.render);
			this.render();
		},
		pageChange: function (e) {
			var currentPage = this.model.get('pageNum');
			var newPage = parseInt($(e.target).text());
			if (newPage !== currentPage) {
				this.gotoPage(newPage);
			}
		},
		pageFirst: function (e) {
			var currentPage = this.model.get('pageNum');
			if (currentPage > 1) {
				this.gotoPage(1);
			}
		},
		pagePrev: function (e) {
			var currentPage = this.model.get('pageNum');
			if (currentPage > 1) {
				this.gotoPage(currentPage - 1);
			}
		},
		pageLast: function (e) {
			var currentPage = this.model.get('pageNum'), pages = this.model
				.get('pages');
			if (currentPage < pages) {
				this.gotoPage(pages);
			}
		},
		pageNext: function (e) {
			var currentPage = this.model.get('pageNum'), pages = this.model
				.get('pages');
			if (currentPage < pages) {
				this.gotoPage(currentPage + 1);
			}
		},
		gotoPage: function (pageNum) {
			this.query.set('pageNum', pageNum);
		},
		render: function () {
			var json = this.model.toJSON();
			json.pagelist = getPageList(json.pageNum, json.pages,
				globalShowCount);
			this.$el.html(this.template(json));
		}
	});

	var setUploader = function (button, url, filefilter) {
		var uploader = new plupload.Uploader({
			browse_button: button[0],
			url: url,
			multi_selection: false,
			filters: filefilter,
			multipart_params: {
				selfCompanyId: COMPANY_ID,
				companyId: COMPANY_ID,
				secretKey: SECRET_KEY,
				excelName: 'orgUserExcel',
				excelUrl: ''
			}
		});
		uploader.bind('BeforeUpload', function (_uploader, file) {
			loader.show();
		});
		uploader.bind('FilesAdded', function (_uploader, files) {
			if (files.length > 0) {
				_uploader.start();
			} else {
				alert('请选择文件').delay(3);
			}
		});
		uploader.bind('FileUploaded', function (_uploader, file, response) {
			if (response.status !== 200) {
				alert('发生错误，文件上传失败！').delay(3);
				return;
			}

			try {
				var result = $.parseJSON(response.response);
				if (result.success) {
					alert(result.message, function () {
						location.reload();
					});
				} else {
					alert(result.message || '导入出错！').delay(3);
				}
			} catch (ex) {
				alert('发生错误，上传失败！').delay(3);
			}

			loader.hide();
		});
		uploader.bind('Error', function (_uploader, error) {
			var message = '未知错误';
			if (typeof error === 'string') {
				message = error;
			} else if (typeof error === 'object') {
				switch (error.code) {
					case -601:
						message = '文件类型不正确，请上传'
							+ filefilter.mime_types[0].extensions + '类型文件';
						break;
					case -600:
						message = '文件大小超出限制，请重新选择';
						break;
					case -400:
					default:
						message = error.message;
						break;
				}
			}
			alert(message).delay(3);
		});
		uploader.init();
		return uploader;
	};

	var itemRender = template($('#tmpl-employeeRow').html()), departModalRender = template($(
		'#tmpl-departEditModal').html()), departMoveRender = template($(
			'#tmpl-departMoveModal').html()), empModalRender = template($(
				'#tmpl-employeeEditModal').html());

	var DeptModal = Backbone.View.extend({
		template: departModalRender,
		className: 'modal',
		events: {
			'click [data-do="save"]': 'doSave'
		},
		initialize: function () {
			this.$el.on('hidden.bs.modal', _.bind(this.hide, this));
			this.$el.appendTo(document.body);
		},
		render: function (data) {
			var markup = this.template(data);
			this.$el.html(markup);

			this.$('form[role=edit-depart]').validate({
				errorClass: 'error-valid',
				rules: {
					'orgName': {
						required: true,
						maxlength: 20
					},
					'showindex': {
						required: true,
						digits: true,
						maxlength: 5
					}
				},
				messages: {
					'orgName': {
						required: '部门名称为必填项',
						maxlength: '最多只能输入20个字符'
					},
					'showindex': {
						required: '排序序号为必填项',
						digits: '必须输入整数',
						maxlength: '最多只能输入5个字符'
					}
				}
			});

			return this;
		},
		doSave: function () {
			var _this = this;
			if (!this.$('form[role=edit-depart]').valid()) {
				return;
			}

			var modelJSON = this.$('form[role=edit-depart]').serializeObject();
			modelJSON.selfCompanyId = COMPANY_ID;
			modelJSON.companyId = COMPANY_ID;
			modelJSON.secretKey = SECRET_KEY;

			$.ajax({
				url: _this.isEdit ? urls.updateDept : urls.saveDept,
				type: 'post',
				dataType: 'json',
				data: modelJSON,
				success: function (res) {
					if (res.success) {
						alert('保存成功!', function () {
							_this.hide();
							_this.trigger('saved', {
								success: true,
								edit: _this.isEdit,
								msg: '',
								model: modelJSON
							});
						}).delay(3);
					} else {
						alert(res.message || '发生错误！').delay(3);
					}
				},
				error: function (jqXHR, textStatus, errorThrown) {
					alert(textStatus + '：' + errorThrown).delay(3);
				}
			});
		},
		show: function (data) {
			this.isEdit = data.edit;
			this.render(data).$el.modal('show');
		},
		hide: function () {
			this.$el.modal('hide');
		}
	});
	var DeptTree = Backbone.View
		.extend({
			events: {
				'click [data-do="addNode"]': 'doAdd',
				'click [data-do="editNode"]': 'doEdit',
				'click [data-do="removeNode"]': 'doRemove'
			},
			initialize: function () {
				var _this = this;
				this.cacheEls();
				this.initSubview();
				this.initData(function (data) {
					_this.initTree(data);
				});
			},
			beforeNodeClick: function (treeId, treeNode, clickFlag) {
				// if (treeNode.isParent) return false;
			},
			onNodeClick: function (event, treeId, treeNode, clickFlag) {
				if ($(event.target).is('.editlink')) {
					var _this = this;
					this.currentNode = treeNode;
					this.$rMenu.css({ // 设置右键菜单的位置
						top: event.clientY + 2 + 'px',
						left: event.clientX + 2 + 'px'
					}).show();
				}

				this.tree.selectNode(treeNode);
				Backbone.trigger('selectDepart', treeNode);

				event.preventDefault();
				event.stopPropagation();
			},
			onNodeRightClick: function (event, treeId, treeNode) {
				this.currentNode = treeNode;
				this.$rMenu.css({ // 设置右键菜单的位置
					top: event.clientY + 2 + 'px',
					left: event.clientX + 2 + 'px'
				}).show();
			},
			initTree: function (data) {
				var _this = this;
				var beforeClick = _.bind(this.beforeNodeClick, this);
				var onClick = _.bind(this.onNodeClick, this);
				// var onRightClick = _.bind(this.onNodeRightClick, this);

				function addDiyDom (treeId, node) {
					// console.log(arguments);
					var tId = node.tId;
					var $node = $('#' + tId);

					var eidtBtn = $('<span class="editlink" style="margin-left: 5px; color: #337ab7" title="修改">修改</span>');
					$node.find('.node_name').after(eidtBtn);
				}

				var setting = {
					view: {
						showLine: false,
						showIcon: false,
						selectedMulti: false,
						autoCancelSelected: false,
						showSelectStyle: true,
						// txtSelectedEnable: false,
						addDiyDom: addDiyDom
					},
					async: {
						url: function (treeId, treeNode) {
							if (!treeNode) {
								return urls.getDepts;
							}
							// else {
							// return urls.getAllDeptOrgId + '/' +
							// COMPANY_ID + '/' + COMPANY_ID +
							// '/' + treeNode.orgId + '/' + SECRET_KEY;
							// }
						},
						enable: true,
						otherParam: ['companyId', COMPANY_ID],
						dataFilter: function (treeId, parentNode, jsonData) {
							if (parentNode == null) {
								if (jsonData.success) {
									return _this.dataParser(jsonData.model);
								}
							} else {
								if (jsonData.success) {
									return _this.dataParser(jsonData.data);
								}
							}
						}
					},
					data: {
						simpleData: {
							enable: true
						}
					},
					edit: {
						enable: false,
						removeTitle: '删除',
						showRemoveBtn: false,
						renameTitle: '编辑',
						showRenameBtn: false,
						drag: {
							isCopy: false,
							prev: false,
							next: false
						}
					},
					callback: {
						beforeClick: beforeClick,
						onClick: onClick
						// onRightClick: onRightClick
					}
				};

				this.tree = $.fn.zTree.init(this.$tree, setting, data);
				this.treeId = this.tree.setting.treeId;

				var rootNode = this.tree.getNodes();
				if (rootNode && rootNode.length > 0) {
					this.tree.selectNode(rootNode[0]);
					this.tree.expandNode(rootNode[0]);
					Backbone.trigger('selectDepart', rootNode[0]);
				}

				this.loadedFinish = true;
			},
			dataParser: function (model) {
				var tempMap = {}, data = [];
				model.sort(function (a, b) {
					return a.showindex - b.showindex;
				});
				_.each(model, function (item) {
					var pId = item.previousId;
					var id = 'dept' + item.id;
					if (pId != '' && pId != null) {
						pId = 'dept' + pId;
					}
					var temp = {
						userType: 'dept',
						isParent: false,
						id: id,
						pId: pId,
						name: item.orgName,
						orgId: item.id,
						previousId: pId,
						showindex: item.showindex
					};

					data.push(temp);
					tempMap[id] = temp;
				});

				var isDataError = false;
				_.each(data, function (item) {
					if (item.pId !== '' && item.pId != null) {
						if (!tempMap[item.pId]) {
							alert('组织结构数据有错误！\r\n请联系管理员！');
							isDataError = true;
							return false;
						}
						tempMap[item.pId].isParent = true;
					}
				});

				return isDataError ? [] : data;
			},
			initData: function (callback) {
				var _this = this;
				$.ajax({
					context: this,
					url: urls.getDepts,
					data: {
						companyId: COMPANY_ID
					},
					success: function (response) {
						_.defaults(response, {
							success: false,
							message: '',
							model: []
						});
						if (response.success) {
							var data = _this.dataParser(response.model);
							callback(data);
						}
					},
					error: function () {
						alert('部门数据获取失败').delay(3);
					}
				});
			},
			initSubview: function () {
				this.subview = {
					editModal: new DeptModal()
				};

				this.listenTo(this.subview.editModal, 'saved', this.doSave);
			},
			cacheEls: function () {
				this.$tree = this.$('.dept-tree');

				var _this = this;
				this.$rMenu = this.$('.right-menu');
				$(document.body)
					.on(
					'click',
					function (e) {
						var el = $(e.target);
						if ((!el.is('.right-menu'))
							&& (el.parents('.right-menu').length == 0)) {
							_this.$rMenu.hide();
							_this.rMenuShow = false;
						}
					});
			},
			doAdd: function (isRoot) {
				var cNode = this.currentNode;
				this.subview.editModal.show({
					edit: false,
					orgName: '',
					orgId: '',
					previousId: isRoot === true ? ''
						: (cNode ? cNode.orgId : ''),
					showindex: 0
				});
				this.$rMenu.hide();
			},
			doEdit: function () {
				var cNode = this.currentNode;
				this.subview.editModal.show({
					edit: true,
					orgName: cNode.name,
					orgId: cNode.orgId,
					previousId: cNode.previousId,
					showindex: cNode.showindex
				});
				this.$rMenu.hide();
			},
			doSave: function (result) {
				var cNode = this.currentNode;
				if (!cNode) cNode = null;
				if (result.success) {
					// if (result.edit) {
					// cNode.name = result.model.orgName;
					// this.tree.updateNode(cNode);
					// } else {
					// this.tree.addNodes(cNode, { name:
					// result.model.orgName });
					this.tree.reAsyncChildNodes(null, 'refresh');
					// }
				}
			},
			doRemove: function () {
				var _this = this, cNode = this.currentNode;
				confirm('确认删除？', function () {
					$.ajax({
						url: urls.deleteDept,
						type: 'post',
						dataType: 'json',
						data: {
							selfCompanyId: COMPANY_ID,
							companyId: COMPANY_ID,
							orgId: cNode.orgId,
							orgName: cNode.orgName,
							secretKey: SECRET_KEY
						},
						success: function (res) {
							if (res.success) {
								alert('删除成功！', function () {
									_this.tree.removeNode(cNode);
									_this.tree.cancelSelectedNode();
								}).delay(3);
							} else {
								alert(res.message || '未知错误').delay(3);
							}
						},
						error: function (jqXHR, textStatus, errorThrown) {
							alert(textStatus + '：' + errorThrown).delay(3);
						}
					});
				});
				this.$rMenu.hide();
			},
			remove: function () {
				if (this.tree) {
					$.fn.zTree.destroy(this.treeId);
				}
				DeptTree.__super__.remove.apply(this, arguments);
			}
		});

	var DeptMoveModal = Backbone.View.extend({
		template: departMoveRender,
		className: 'modal',
		events: {
			'click [data-do="save"]': 'doSave'
		},
		initialize: function () {
			this.$el.on('hidden.bs.modal', _.bind(this.hide, this));
			this.$el.appendTo(document.body);

			this.render();
			this.$tree = this.$('#move-tree');
			this.initData();

			var fheight = parseInt($(document).height() * 0.6);
			this.$('.dept-move-body').css({
				'height': fheight + 'px',
				'overflow': 'auto'
			});
		},
		render: function () {
			var markup = this.template();
			this.$el.html(markup);
			return this;
		},
		initTree: function (data) {
			var _this = this;
			var setting = {
				view: {
					showLine: false,
					showIcon: false,
					selectedMulti: false,
					autoCancelSelected: false,
					showSelectStyle: true
				},
				async: {
					url: function (treeId, treeNode) {
						if (!treeNode) {
							return urls.getDepts;
						}
						// else {
						// return urls.getAllDeptOrgId + '/' + COMPANY_ID + '/'
						// + COMPANY_ID +
						// '/' + treeNode.orgId + '/' + SECRET_KEY;
						// }
					},
					enable: true,
					otherParam: ['companyId', COMPANY_ID],
					dataFilter: function (treeId, parentNode, jsonData) {
						if (parentNode == null) {
							if (jsonData.success) {
								return _this.dataParser(jsonData.model);
							}
						} else {
							if (jsonData.success) {
								return _this.dataParser(jsonData.data);
							}
						}
					}
				},
				data: {
					simpleData: {
						enable: true
					}
				}
			};

			this.tree = $.fn.zTree.init(this.$tree, setting, data);
			this.treeId = this.tree.setting.treeId;

			var rootNode = this.tree.getNodes()[0];
			this.tree.selectNode(rootNode);
			this.tree.expandNode(rootNode);

			this.loadedFinish = true;
		},
		dataParser: function (model) {
			var tempMap = {}, data = [];
			model.sort(function (a, b) {
				return a.showindex - b.showindex;
			});
			_.each(model, function (item) {
				var pId = item.previousId;
				var id = 'dept' + item.id;
				if (pId != '' && pId != null) {
					pId = 'dept' + pId;
				}
					
				var temp = {
					userType: 'dept',
					isParent: false,
					id: id,
					pId: pId,
					name: item.orgName,
					orgId: item.id,
					previousId: pId,
					showindex: item.showindex
				};

				data.push(temp);
				tempMap[id] = temp;
			});
			_.each(data, function (item) {
				if (item.pId !== '' && item.pId != null) {
					tempMap[item.pId].isParent = true;
				}
			});

			return data;
		},
		initData: function () {
			// var
			$.ajax({
				context: this,
				url: urls.getDepts,
				data: {
					companyId: COMPANY_ID
				},
				success: function (response) {
					_.defaults(response, {
						success: false,
						message: '',
						model: []
					});
					if (response.success) {
						var data = this.dataParser(response.model);
						this.initTree(data);
					}
				},
				error: function () {
					alert('部门数据获取失败').delay(3);
				}
			});
		},
		doSave: function () {
			var _this = this;
			var data = this.currentDate;
			var cNode = _this.tree.getSelectedNodes()[0];
			if (!cNode) {
				alert('请选择要移动的部门！').delay(3);
				return;
			}

			data = _.pick(data, 'userId', 'userName', 'post', 'workNumber',
				'showindex', 'orgId');
			data.orgId = cNode.orgId;
			data.selfCompanyId = COMPANY_ID;
			data.companyId = COMPANY_ID;
			data.isManager = 0;
			data.secretKey = SECRET_KEY;

			$.ajax({
				url: urls.updateDeptByUserId,
				type: 'post',
				dataType: 'json',
				data: data,
				success: function (res) {
					if (res.success) {
						alert('保存成功!', function () {
							_this.hide();
							_this.trigger('modify', {
								success: true
							});
						}).delay(3);
					} else {
						alert(res.message || '未知错误').delay(3);
					}
				},
				error: function (jqXHR, textStatus, errorThrown) {
					alert(textStatus + '：' + errorThrown).delay(3);
				}
			});
		},
		show: function (data) {
			this.tree.cancelSelectedNode();
			this.tree.expandAll(false);
			this.$el.modal('show');

			this.currentDate = data;
		},
		hide: function () {
			this.$el.modal('hide');
		}
	});

	var EmployeeModal = Backbone.View.extend({
		template: empModalRender,
		className: 'modal',
		events: {
			'click [data-do="save"]': 'doSave'
		},
		initialize: function () {
			this.$el.on('hidden.bs.modal', _.bind(this.hide, this));
			this.$el.appendTo(document.body);
		},
		render: function (data) {
			var markup = this.template(data);
			this.$el.html(markup);

			this.$('form[role=edit-employee]').validate({
				errorClass: 'error-valid',
				rules: {
					'userName': {
						required: true,
						maxlength: 20
					},
					'mobile': {
						required: true,
						mobile: true
					},
					'post': {
						maxlength: 20
					},
					'workNumber': {
						maxlength: 20
					},
					'showindex': {
						required: true,
						digits: true,
						maxlength: 5
					}
				},
				messages: {
					'userName': {
						required: '用户名为必填项',
						maxlength: '最多只能输入20个字符'
					},
					'mobile': {
						required: '手机为必填项',
						mobile: '手机号码不正确'
					},
					'post': {
						maxlength: '最多只能输入20个字符'
					},
					'workNumber': {
						maxlength: '最多只能输入20个字符'
					},
					'showindex': {
						required: '排序序号为必填项',
						digits: '必须输入整数',
						maxlength: '最多只能输入5个字符'
					}
				}
			});
			return this;
		},
		doSave: function () {
			var _this = this;
			if (!this.$('form[role=edit-employee]').valid()) {
				return;
			}

			var modelJSON = this.$('form[role=edit-employee]')
				.serializeObject();
			modelJSON.selfCompanyId = COMPANY_ID;
			modelJSON.companyId = COMPANY_ID;
			modelJSON.isManager = 0;
			modelJSON.secretKey = SECRET_KEY;

			$.ajax({
				url: _this.isEdit ? urls.updateUser : urls.saveUser,
				type: 'post',
				dataType: 'json',
				data: modelJSON,
				success: function (res) {
					if (res.success) {
						alert('保存成功!', function () {
							_this.hide();
							_this.trigger('modify', {
								success: true,
								edit: _this.isEdit,
								msg: '',
								model: modelJSON
							});
						}).delay(3);
					} else {
						alert(res.message || '未知错误').delay(3);
					}
				},
				error: function (jqXHR, textStatus, errorThrown) {
					alert(textStatus + '：' + errorThrown).delay(3);
				}
			});
		},
		show: function (data) {
			this.isEdit = data.edit;
			this.render(data).$el.modal('show');
		},
		hide: function () {
			this.$el.modal('hide');
		}
	});
	var EmployeeModel = Backbone.Model.extend({
		defaults: {
			userId: '',
			userName: '',
			mobile: ''
		},
		pending: false,
		destroy: function () {
			if (this.pending) {
				return false;
			}

			var data = this.toJSON();
			$.ajax({
				context: this,
				url: urls.deleteUser,
				type: 'post',
				dataType: 'json',
				data: {
					selfCompanyId: COMPANY_ID,
					companyId: COMPANY_ID,
					userId: data.id, // data.userId,
					userName: data.userName,
					secretKey: SECRET_KEY
				},
				beforeSend: function () {
					this.pending = true;
				},
				success: function (resp) {
					_.defaults(resp, {
						success: false,
						message: '未知错误'
					});
					if (resp.success) {
						this.stopListening();
						this.trigger('destroy', this, this.collection);
					} else {
						alert(resp.message).delay(3);
					}
				},
				error: function (jqXHR, textStatus, errorThrown) {
					alert(textStatus + '：' + errorThrown).delay(3);
				},
				complete: function () {
					this.pending = false;
				}
			});
		}
	});
	var EmployeeCollection = Backbone.Collection.extend({
		model: EmployeeModel,
		url: urls.getDeptUsers,
		parse: function (resp) {
			var data = [];
			if (_.isObject(resp) && _.isArray(resp.model)) {
				resp.model.sort(function (a, b) {
					return a.showindex - b.showindex;
				});
				data = resp.model;
			}
			return data;
		},
		filter: function (query) {
			query.rnd = (new Date).getTime();
			this.fetch({
				reset: true,
				data: query
			});
		}
	});
	var EmployeeItemView = Backbone.View.extend({
		tagName: 'tr',
		template: itemRender,
		events: {
			'click [data-do="delete"]': 'doDelete',
			'click [data-do="edit"]': 'doEdit',
			'click [data-do="move"]': 'doMove',
		},
		initialize: function () {
			this.listenTo(this.model, 'remove', this.remove);
			this.listenTo(this.model, 'change', this.render);
		},
		doDelete: function () {
			var model = this.model;
			confirm('确认删除？', function () {
				model.destroy();
			});
		},
		doEdit: function () {
			this.model.trigger('setting', this.model);
		},
		doMove: function () {
			this.model.trigger('movesetting', this.model);
		},
		render: function () {
			var markup = this.template(this.model.toJSON());
			this.$el.html(markup);
			return this;
		}
	});
	var EmployeeTable = Backbone.View.extend({
		initialize: function () {
			this.listenTo(this.collection, 'reset', this.reset);
			this.cacheEls();
		},
		addOne: function (model, collection, options) {
			var itemView = new EmployeeItemView({
				model: model
			});
			this.$items.append(itemView.render().el);
		},
		reset: function (collection, options) {
			var previousModels = options.previousModels;
			_.each(previousModels, function (model) {
				model.trigger('remove');
			});

			this.$items.empty();

			collection.each(function (model, index) {
				model.set({
					$index: index
				});
				this.addOne(model, collection);
			}, this);
		},
		cacheEls: function () {
			this.$items = this.$('[role="items"]');
		}
	});

	var run = function () {
		var defaultQuery = {
			pageNum: 1,
			pageSize: 10,
			deptId: '',
			companyId: COMPANY_ID,
			isPage: 1
		};
		var searchQuery = new Backbone.Model(defaultQuery);
		var pageModel = new PageModel();

		var deptt = new DeptTree({
			el: $('[role=depts]')
		});
		var empList = new EmployeeCollection();
		empList.parse = function (resp) {
			var data = [];
			if (_.isObject(resp) && _.isObject(resp.model) && resp.success) {
				data = resp.model.list;
				pageModel.set({
					pageNum: resp.model.pageNum,
					pageSize: resp.model.pageSize,
					pages: resp.model.pages < 1 ? 1 : resp.model.pages,
					total: resp.model.total
				});
				pageModel.trigger('response');
			}
			return data;
		};
		var empTable = new EmployeeTable({
			el: '#table-employee',
			collection: empList
		});

		var pageView = new PageView({
			el: '.panel-footer',
			model: pageModel,
			query: searchQuery
		});
		searchQuery.on('change', function (model, option) {
			empList.filter(model.toJSON());
		});

		var deptMoveModal = new DeptMoveModal();
		var empEditModal = new EmployeeModal();

		empList.on('setting', function (modal) {
			var data = modal.toJSON();
			data.edit = true;
			data.userId = data.id;
			// data.orgId = data.org.id;
			data.workNumber = data.workNumber;
			empEditModal.show(data);
		});
		empList.on('movesetting', function (modal) {
			var data = modal.toJSON();
			data.edit = true;
			// data.orgId = data.org.id;
			data.userId = data.id;
			data.workNumber = data.workNumber;
			deptMoveModal.show(data);
		});
		$('.btn-add').on('click', function () {
			if (deptt.tree.getSelectedNodes().length === 0) {
				alert('请先选择一个部门！');
				return;
			}
			empEditModal.show({
				userId: '',
				userName: '',
				mobile: '',
				orgId: deptt.tree.getSelectedNodes()[0].orgId,
				post: '',
				workNumber: '',
				edit: false,
				showindex: 0
			});
		});
		Backbone.listenTo(empEditModal, 'modify', function (result) {
			if (result.success) {
				var dept = deptt.tree.getSelectedNodes()[0];
				searchQuery.set({
					deptId: dept.orgId
				});
			}
		});
		Backbone.listenTo(deptMoveModal, 'modify', function (result) {
			if (result.success) {
				var dept = deptt.tree.getSelectedNodes()[0];
				searchQuery.set({
					deptId: dept.orgId
				});
			}
		});
		Backbone.listenTo(Backbone, 'selectDepart', function (dept) {
			searchQuery.set(_.extend({}, defaultQuery, {
				deptId: dept.orgId
			}));
		});

		Backbone.listenTo(deptt.subview.editModal, 'saved', function () {
			deptMoveModal.tree.reAsyncChildNodes(null, 'refresh');
		});
		$('.btn-add-dept').on('click', function () {
			deptt.doAdd(true);
		});

		setUploader($('.btn-import'), urls.import, {
			mime_types: [{
				title: 'Excel文件',
				extensions: 'xls,xlsx'
			}],
			max_file_size: '10240kb'
		});

		$('.btn-check').on('click', function () {
			confirm('确认同步人员信息？', function () {
				$.ajax({
					url: CONTEXT_PATH + '/vwt/synchroVwtData.do',
					beforeSend: function () {
						$('.btn-check').prop('disabled', true);
						loader.show();
					},
					success: function (resp) {
						resp = _.extend({
							success: false,
							message: '操作失败'
						}, resp);

						if (resp.success) {
							alert('同步成功');
							window.location.reload();
						} else {
							alert(resp.message);
						}
					},
					error: function () {
						alert('请求失败');
					},
					complete: function () {
						$('.btn-check').prop('disabled', false);
						loader.hide();
					}
				});
			});
		});
	};

	module.exports = {
		run: run
	};
});